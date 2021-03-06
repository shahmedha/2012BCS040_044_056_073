#!/bin/sh
#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.inf.usi.ch/carzaniga/siena/
#
#  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-2002 University of Colorado
#
#  Siena is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#  
#  Siena is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License
#  along with Siena.  If not, see <http://www.gnu.org/licenses/>.
#
. ./config.sh
#
test_echo Testing communication performance...
#
cleanup_files='test.in1 test.out test-s.out test-s.in sender.log raddr test.in'
#
# usage: test_performance ka|udp|tcp|ssl <receiver-threads> <sender-count> [<slow-senders> <sender-pause>]
#
test_performance() {
#    prf='-Xrunhprof:cpu=samples,file=javaprof.txt'
    prf=''
    scount=$3
    sscount=$4
    slack=$5
    cleanup_procs=''
#
    test_description "$1: $2 threads, $scount fast senders"
    case "$1" in 
	ssl )
	    sslrcvparams="-Djavax.net.ssl.keyStore=$srcdir/sienakeystore.jks -Djavax.net.ssl.keyStorePassword=sienapassword"
	    sslsndparams="-Djavax.net.ssl.trustStore=$srcdir/sienakeystore.jks -Djavax.net.ssl.trustStorePassword=sienapassword"
	    ;;
	* )
	    sslrcvparams=''
	    sslsndparams=''
	    ;;
    esac
    rm -f raddr
    $JAVA $sslrcvparams $prf sienatest.TestPacketReceiver -perf $1:7878:localhost:raddr $2 \
	> test.out &
    clientpid=$!
    cleanup_procs="$cleanup_procs $!"
    sleep 4
    i=5
    while test ! -f raddr -a $i != 0; do
	sleep 4
	i=`expr $i - 1`
    done
    address=`cat raddr`
    senders=''
    ssenders=''
    input=''
    while test "$scount" != 0; do 
	$JAVA $sslsndparams sienatest.TestPacketSender -quiet "$address" \
	    < test.in &
	senders="$senders $!"
	input="$input test.in"
	scount=`expr $scount - 1`
    done
    if test -n "$sscount"; then
	test -n "$slack" || slack=0
	test_description ", $sscount slow senders"
	while test "$sscount" != 0; do 
	    $JAVA $sslsndparams sienatest.TestPacketSender \
		-quiet "$address" $slack < test.in > /dev/null 2>&1 &
	    ssenders="$ssenders $!"
	    cleanup_procs="$cleanup_procs $!"
	    input="$input test.in"
	    sscount=`expr $sscount - 1`
	done
    fi
    wait $senders
    senders=''
    test_description ': '
    echo "---FINE---" | $JAVA $sslsndparams sienatest.TestPacketSender -quiet "$address"
    wait $clientpid
    clientpid=''
    test_cat test.out
#    cat javaprof.txt
    if test -n "$ssenders"; then
	kill $ssenders
	wait $ssenders
	ssenders=''
    fi	
    cleanup_procs=''
    rm -f test.out test-s.out test-s.in sender.log raddr
}
#
#  I should do a better job at generating the input file here ...
#
cat > test.in1 <<EOF
.
./win32api
./win32api/RCS
./win32api/include
./win32api/include/siena
./win32api/include/siena/RCS
./win32api/include/siena/Comm.h
./win32api/include/siena/Comm.icc
./win32api/include/siena/SENP.h
./win32api/include/siena/SENP.icc
./win32api/include/siena/Siena.h
./win32api/include/siena/Siena.icc
./win32api/include/siena/SienaId.h
./win32api/include/siena/ThinClient.h
./win32api/include/siena/ThinClient.icc
./win32api/include/sienaconf.h
./win32api/include/RCS
./win32api/Makefile-win32
./win32api/sienaconf-win32.h
./win32api/Makefile
./win32api/SENP.cc
./win32api/SENPTokenizer.h
./win32api/Siena.cc
./win32api/SienaId.cc
./win32api/ThinClient.cc
./win32api/publisher.cc
./win32api/subscriber.cc
./win32api/README.win32
./win32api/Comm.cc
./c++api
./c++api/RCS
./c++api/include
./c++api/include/siena
./c++api/include/siena/RCS
./c++api/include/siena/Comm.h
./c++api/include/siena/Comm.icc
./c++api/include/siena/SENP.h
./c++api/include/siena/SENP.icc
./c++api/include/siena/Siena.h
./c++api/include/siena/Siena.icc
./c++api/include/siena/SienaId.h
./c++api/include/siena/ThinClient.h
./c++api/include/siena/ThinClient.icc
./c++api/include/siena/Siena.h.flc
./c++api/include/RCS
./c++api/configure
./c++api/AUTHORS
./c++api/Comm.cc
./c++api/LICENSE
./c++api/Makefile.in
./c++api/README
./c++api/SENP.cc
./c++api/SENPTokenizer.h
./c++api/Siena.cc
./c++api/SienaId.cc
./c++api/ThinClient.cc
./c++api/VERSION
./c++api/aclocal.m4
./c++api/antodoc.css
./c++api/configure.in
./c++api/doxygen.conf.in
./c++api/footer.html
./c++api/header.html.in
./c++api/install-sh
./c++api/mkinstalldirs
./c++api/publisher.cc
./c++api/sienaconf.in
./c++api/subscriber.cc
./c++api/tests
./c++api/tests/RCS
./c++api/tests/Makefile.in
./c++api/tests/TestTokenizer.cc
./c++api/tests/TestPacketReader.cc
./c++api/tests/TestSender.cc
./c++api/tests/TestTCPReceiver.cc
./c++api/tests/TestUDPReceiver.cc
./c++api/tests/TestPublisher.cc
./c++api/tests/TestSubscriber.cc
./c++api/tests/test_tokenizer.sh
./c++api/tests/test_packet_reader.sh
./c++api/tests/test_comm.sh
./c++api/tests/test_thinclient.sh
./c++api/docspack.tar.gz
./c++api/fdl.txt
./c++api/README.win32
./c++api/sienaconf-win32.h
./c++api/configure.flc
./c++api/ChangeLog
./c++api/Makefile-win32-bin
./c++api/Makefile-win32.in
./java
./java/RCS
./java/Op.java
./java/Makefile.in
./java/AUTHORS
./java/LICENSE
./java/configure.in
./java/README
./java/VERSION
./java/AttributeValue.java
./java/Logging.java
./java/Siena.java
./java/AttributeConstraint.java
./java/Filter.java
./java/SienaException.java
./java/Notifiable.java
./java/Covering.java
./java/SENP.java
./java/SENPPacket.java
./java/PacketReceiver.java
./java/configure
./java/Pattern.java
./java/lesser.txt
./java/TCPUDPSenderFactory
./java/TODO
./java/PacketSenderFactory.java
./java/TCPPacketReceiver.java
./java/makehex
./java/testpkg.sh
./java/Monitor.java
./java/SienaId.java
./java/StartServer.java
./java/aclocal.m4
./java/install-sh
./java/stylesheet.css
./java/SOFFITTA
./java/SOFFITTA/TestThinClientPub.java
./java/SOFFITTA/test_string.sh
./java/SOFFITTA/test.sh
./java/SOFFITTA/rick_test.sh
./java/SOFFITTA/SingleThinClient.java
./java/SOFFITTA/Makefile,v
./java/SOFFITTA/ThinClient.java
./java/SOFFITTA/Siena.java
./java/fdl.txt
./java/sp
./java/sp/test
./java/sp/SP.java
./java/sp/tf
./java/sp/test.c
./java/sp/Test.class
./java/sp/Test.java
./java/sp/tfj
./java/sp/Makefile.in
./java/sp/SPObject.java
./java/sp/SPPacket.java
./java/sp/Notification.java
./java/sp/configure.in
./java/sp/aclocal.m4
./java/sp/autom4te.cache
./java/sp/autom4te.cache/output.0
./java/sp/autom4te.cache/traces.0
./java/sp/autom4te.cache/requests
./java/sp/configure
./java/sp/tests
./java/sp/tests/Makefile.in
./java/sp/tests/TestInts.java
./java/sp/tests/config.sh.in
./java/sp/tests/test_ints.sh
./java/sp/tests/RCS
./java/sp/tests/TestWriteInts.java
./java/sp/tests/test_write_ints.sh
./java/sp/VERSION
./java/sp/RCS
./java/sp/SPIndex.java
./java/sp/SPString.java
./java/sp/SPByteString.java
./java/ThinClient.java
./java/tests
./java/tests/test_covering.sh
./java/tests/Makefile.in
./java/tests/RCS
./java/tests/TestLocalPubSub.java
./java/tests/base.sh
./java/tests/InterestedParty.java
./java/tests/TestTokenizer.java
./java/tests/test_patterns.sh
./java/tests/TestBase.java
./java/tests/test_simple_sub.sh
./java/tests/test_tokenizer.sh
./java/tests/Compare.java
./java/tests/Sender.java
./java/tests/topology.sh
./java/tests/Receiver.java
./java/tests/TestCovering.java
./java/tests/TestDecode.java
./java/tests/test_configure.sh
./java/tests/test_clearsubs.sh
./java/tests/test_remote_sub.sh
./java/tests/test_filter.sh
./java/tests/TestEncode.java
./java/tests/TestFilter.java
./java/tests/PerformanceTest.java
./java/tests/topology_single.sh
./java/tests/test_encode.sh
./java/tests/test_shutdown.sh
./java/tests/test_performances.sh
./java/tests/TestThinClient.java
./java/tests/test_remote_patterns.sh
./java/tests/config.sh.in
./java/tests/TestNotificationBuffer.java
./java/tests/TestPatterns2.java
./java/tests/PatternReader.java
./java/tests/TestPatterns.java
./java/tests/TestPacketReceiver.java
./java/tests/SubscriberCounter.java
./java/tests/test_packet_receiver.sh
./java/tests/TestShutdown.java
./java/tests/test_patterns2.sh
./java/tests/ObjectOfInterest.java
./java/tests/test_thinclient.sh
./java/tests/PerformanceReceiver.java
./java/tests/test_notif_buffer.sh
./java/tests/TestClearSubscriptions
./java/tests/PerformanceSender.java
./java/tests/TestTCPPacketReceiver.java
./java/tests/TestClearSubscriptions.java
./java/tests/test_comm_performance.sh
./java/tests/TestPacketSender.java
./java/tests/test_remove_unreachables.sh
./java/tests/TestReceiverPerformance.java
./java/tests/test_unsubscriptions.sh
./java/tests/TestSenderPerformance.java
./java/tests/test_ka_packet_receiver.sh
./java/tests/TestReceiverShutdown.java
./java/tests/test_encode_performance.sh
./java/tests/TestEncodePerformance.java
./java/tests/test_receiver_shutdown.sh
./java/Util.java
./java/Notification.java
./java/PacketReceiverException.java
./java/DirectSENPInterface.java
./java/NotificationBuffer.java
./java/overview.html
./java/PacketReceiverClosed.java
./java/HierarchicalDispatcher.java
./java/AttributeName.java
./java/PacketSender.java
./java/ChangeLog
./java/Notif.java
./java/AttributeMap.java
./java/PacketSenderException.java
./java/TCPPacketSender.java
./java/UDPPacketReceiver.java
./java/TCPKeepAliveReceiver.java
./java/makehex.c
./java/UDPPacketSender.java
./java/TCPUDPSenderFactory.java
./java/SelectorPacketReceiver.java
./java/TCPUDPPacketSenderFactory.java
./java/InvalidSenderException.java
./java/KAPacketReceiver.java
./java/SequentialDispatcher.java
./java/PacketReceiverFatalError.java
./java/KAProtocol.java
./java/KAPacketSender.java
./java/SENPInvalidFormat.java
./java/TimeoutExpired.java
./java/GenericSenderFactory.java
./slib
./slib/RCS
./slib/tests
./slib/tests/RCS
./slib/tests/Makefile.in
./slib/tests/echo.cc
./slib/tests/httpd.cc
./slib/tests/interested_party.cc
./slib/tests/object_of_interest.cc
./slib/tests/www.cc
./slib/include
./slib/include/siena
./slib/include/siena/RCS
./slib/include/siena/IO.h
./slib/include/siena/SENP.h
./slib/include/siena/SienaIO.h
./slib/include/siena/Socket.h
./slib/include/siena/Monitor.h
./slib/include/siena/Siena.h
./slib/include/siena/SimpleSiena.h
./slib/include/siena/URI.h
./slib/include/siena/IO.icc
./slib/include/siena/URI.icc
./slib/include/siena/SENP.icc
./slib/include/siena/Siena.icc
./slib/include/siena/Socket.icc
./slib/include/RCS
./slib/include/siena.h
./slib/Date.cc
./slib/Makefile.in
./slib/Monitor.cc
./slib/SENP.cc
./slib/Siena.cc
./slib/SienaIO.cc
./slib/SimpleSiena.cc
./slib/Socket.cc
./slib/URI.cc
./slib/VERSION
./slib/aclocal.m4
./slib/classHeader.html
./slib/configure.in
./slib/footer.html
./slib/hierHeader.html
./slib/index.html
./slib/indexHeader.html
./slib/sienaconf.in
./slib/srcinstall.html
./slib/configure
./slib/install-sh
./slib/c++doc.tar.gz
./smon
./smon/Graph.cc
./smon/Graph.h
./smon/Image.cc
./smon/Image.h
./smon/Makefile.in
./smon/RCS
./smon/smon.spec
./smon/configure.in
./smon/main.cc
./smon/monitor.cc
./smon/monitor.h
./smon/graph.h
./smon/gui.h
./smon/gui.h.flc
./smon/smonconf.in
./smon/smon.h
./smon/smon.cc
./smon/graph.cc
./smon/configure
./smon/AUTHORS
./smon/NodesEdges.h
./smon/Rectangle.h
./smon/VERSION
./smon/README
./smon/install-sh
./smon/glade
./smon/glade/smon.glade
./smon/glade/smon.glade.bak
./smon/glade/src
./smon/glade/src/support.h
./smon/glade/src/support.c
./smon/glade/src/interface.h
./smon/glade/src/interface.c
./smon/glade/src/callbacks.h
./smon/glade/src/callbacks.c
./smon/glade/RCS
./smon/glade/pixmaps
./smon/glade/pixmaps/smon.xpm
./smon/glade/smon.glade.flc
./smon/Graph.old.cc
./smon/Icons.h
./smon/aclocal.m4
./smon/pixmaps
./smon/pixmaps/siena-ss.png
./smon/pixmaps/smon.xcf
./smon/pixmaps/.xvpics
./smon/pixmaps/.xvpics/smon.xcf
./smon/pixmaps/.xvpics/smon.xpm
./smon/pixmaps/.xvpics/smon.png
./smon/pixmaps/smon.xpm
./smon/pixmaps/smon.png
./smon/pixmaps/adapt.xpm
./smon/pixmaps/adv.xpm
./smon/pixmaps/anto.xpm
./smon/pixmaps/constrain.xpm
./smon/pixmaps/inventory.xpm
./smon/pixmaps/notif.xpm
./smon/pixmaps/obj1.xpm
./smon/pixmaps/obj2.xpm
./smon/pixmaps/pub.xpm
./smon/pixmaps/reconfigure.xpm
./smon/pixmaps/remove.xpm
./smon/pixmaps/rick.xpm
./smon/pixmaps/siena.xpm
./smon/pixmaps/sub.xpm
./smon/pixmaps/unadv.xpm
./smon/pixmaps/unsub.xpm
./smon/pixmaps/update.xpm
./smon/Graph.old.h
./smon/Graph.icc
./smon/Icons.cc
./smon/SimpleGraph.h
./smon/geometry.icc
./smon/NodesEdges.cc
./smon/SimpleGraph.cc
./smon/test.txt
./smon/geometry.h
./smon/SimpleGraph.icc
./smon/LICENSE
./smon/Animation.h
./smon/mkinstalldirs
./sp
./sp/LICENSE
./sp/sp.h
./sp/sp.cc
./sp/mutable.cc
./sp/sienaconf.in
./sp/configure.in
./sp/AUTHORS
./sp/RCS
./sp/Makefile.in
./sp/install-sh
./sp/mkinstalldirs
./sp/INSTALL
./sp/VERSION
./sp/tests
./sp/tests/Makefile.in
./sp/tests/RCS
./sp/tests/CVS
./sp/tests/CVS/Root
./sp/tests/CVS/Repository
./sp/tests/CVS/Entries
./sp/tests/string.sh
./sp/tests/test_sp_string.cc
./sp/tests/sp_event.sh
./sp/tests/name.sh
./sp/tests/test_sp_name.cc
./sp/tests/test_mutable_value.cc
./sp/tests/mutable_value.sh
./sp/tests/test_mutable_event.cc
./sp/tests/mutable_event.sh
./sp/tests/test_sp_event.cc
./sp/tests/test_sp_value.cc
./sp/tests/sp_value.sh
./sp/tests/test_unions.cc
./sp/tests/test_sptypes.cc
./sp/tests/sptypes.sh
./sp/tests/test_spmutable.cc
./sp/tests/spmutable.sh
./sp/tests/test_find.cc
./sp/tests/find.sh
./sp/CVS
./sp/CVS/Root
./sp/CVS/Repository
./sp/CVS/Entries
./sp/configure
./sp/mutable.h
./sp/README
./sp/spconf.in
./sp/mutable.icc
./sp/autom4te.cache
./sp/autom4te.cache/output.0
./sp/autom4te.cache/traces.0
./sp/autom4te.cache/requests
./sp/autom4te.cache/output.1
./sp/autom4te.cache/traces.1
./sp/sp.icc
./sp/sptypes.icc
./sp/sptypes.h.flc
./sp/Makefile.am
./sp/sptypes.icc.flc
./sp/aclocal.m4
./sp/sptypes.h
./sp/sptypes.cc
./sp/spmutable.h
./sp/spmutable.icc
./sp/spmutable.cc
./server
./server/RCS
./server/CoveringRelations.icc
./server/Object.h
./server/CoveringRelations.cc
./server/Db.cc
./server/Db.h
./server/Log.h
./server/PThreads.cc
./server/PThreads.h
./server/PThreads.icc
./server/index.html
./server/CoveringRelations.h
./server/main.cc
./server/srcinstall.html
./server/set_util.h
./server/EventServer.cc
./server/configure.in
./server/INSTALL
./server/LICENSE
./server/AUTHORS
./server/aclocal.m4
./server/VERSION
./server/install-sh
./server/configure
./server/tests
./server/tests/Makefile.in
./server/tests/base.sh
./server/tests/test_any.sh
./server/tests/RCS
./server/tests/test_selector.cc
./server/tests/test_cover_direct.sh
./server/tests/test_cover.sh
./server/tests/test_db.sh
./server/tests/test_float.sh
./server/tests/test_integer.sh
./server/tests/test_net1.sh
./server/tests/test_net.sh
./server/tests/test_string.sh
./server/tests/topology4.sh
./server/tests/config.in
./server/tests/client.cc
./server/tests/compare.cc
./server/tests/sender.cc
./server/tests/killtopology.sh
./server/tests/covers.cc
./server/tests/pingsiena.cc
./server/tests/topology1.sh
./server/tests/topology2.sh
./server/PostOffice.h
./server/sienaserverconf.in
./server/EventServer.h
./server/i386-gnu-linux
./server/README
./server/GenericServer.h
./server/Makefile.in
./server/PostOffice.cc
./server/PostOffice.icc
./server/PoSet.h
./server/PoSet.cc
./server/Selector.cc
./server/Selector.h
./server/SienaId.icc
./server/Selector.icc
./server/ReactiveHandler.cc
./server/ReactiveHandler.h
./server/ReactiveHandler.icc
./server/Time.h
./server/Time.icc
./server/SienaId.h
./server/SienaId.cc
./server/PoSet.icc
./servercpp
./servercpp/RCS
./servercpp/tests
./servercpp/tests/RCS
./servercpp/tests/Makefile.in
./servercpp/tests/base.sh
./servercpp/tests/client.cc
./servercpp/tests/compare.cc
./servercpp/tests/config.in
./servercpp/tests/covers.cc
./servercpp/tests/killtopology.sh
./servercpp/tests/pingsiena.cc
./servercpp/tests/sender.cc
./servercpp/tests/test_any.sh
./servercpp/tests/test_cover.sh
./servercpp/tests/test_cover_direct.sh
./servercpp/tests/test_db.sh
./servercpp/tests/test_float.sh
./servercpp/tests/test_integer.sh
./servercpp/tests/test_net.sh
./servercpp/tests/test_net1.sh
./servercpp/tests/test_string.sh
./servercpp/tests/topology1.sh
./servercpp/tests/topology2.sh
./servercpp/tests/topology4.sh
./servercpp/AUTHORS
./servercpp/CoveringRelations.cc
./servercpp/CoveringRelations.h
./servercpp/CoveringRelations.icc
./servercpp/Db.cc
./servercpp/Db.h
./servercpp/EventServer.cc
./servercpp/EventServer.h
./servercpp/LICENSE
./servercpp/Makefile.in
./servercpp/PThreads.cc
./servercpp/PThreads.h
./servercpp/PThreads.icc
./servercpp/README
./servercpp/VERSION
./servercpp/aclocal.m4
./servercpp/configure.in
./servercpp/index.html
./servercpp/install-sh
./servercpp/main.cc
./servercpp/set_util.h
./servercpp/sienaserverconf.in
./servercpp/srcinstall.html
./servercpp/configure
./benchmarks
./benchmarks/phil
./benchmarks/phil/BenchPub.java
./benchmarks/phil/BenchSub.java
./benchmarks/phil/BenchPub.class
./benchmarks/phil/BenchSub.class
./benchmarks/phil/output
./benchmarks/phil/run_test.sh
./benchmarks/phil/java.hprof.txt
./sslcomm
./sslcomm/Inventory
./sslcomm/README
./sslcomm/Makefile
./sslcomm/test_ssl.sh
./sslcomm/sienakeys
./sslcomm/SSLInterestedParty.java
./sslcomm/SSLObjectOfInterest.java
./sslcomm/PacketSenderFactory.java
./sslcomm/SSLPacketReceiver.java
./sslcomm/SSLPacketSender.java
./sslcomm/StartServer.java
./siena-ssl.tar.gz
./siena_ssl.tar.gz
./naive_matching
./naive_matching/forwarding.cc
./naive_matching/matching.cc
./naive_matching/notification.cc
./naive_matching/predicate.cc
./naive_matching/forwarding.h
./naive_matching/matching.h
./naive_matching/notification.h
./naive_matching/predicate.h
./naive_matching/missing
./naive_matching/nfile1
./naive_matching/filters3
./naive_matching/configure
./naive_matching/aclocal.m4
./naive_matching/Makefile.in
./naive_matching/install-sh
./naive_matching/NEWS
./naive_matching/Makefile.am
./naive_matching/configure.in
./naive_matching/VERSION
./naive_matching/mkinstalldirs
./naive_matching/INSTALL
./naive_matching/COPYING
./naive_matching/README
./naive_matching/AUTHORS
./naive_matching/ChangeLog
./matching
./matching/configure
./matching/configure.in
./matching/ConstraintSet.h
./matching/VERSION
./matching/Makefile.in
./matching/counting.tar
./matching/BitVector.h
./matching/tmp
./matching/tmp/aclocal.m4
./matching/tmp/AUTHORS
./matching/tmp/forwarding.cc
./matching/tmp/Le.cc
./matching/tmp/matching.cc
./matching/tmp/notification.cc
./matching/tmp/predicate.cc
./matching/tmp/missing
./matching/tmp/forwarding.h
./matching/tmp/Le.h
./matching/tmp/matching.h
./matching/tmp/notification.h
./matching/tmp/predicate.h
./matching/tmp/COPYING
./matching/tmp/Makefile
./matching/tmp/VERSION
./matching/tmp/README
./matching/tmp/nfile1
./matching/tmp/nfile1.bak
./matching/tmp/nfile1.bak2
./matching/tmp/nfile1.file3
./matching/tmp/filters3.1
./matching/tmp/filters4.1
./matching/tmp/INSTALL
./matching/tmp/install-sh
./matching/tmp/mkinstalldirs
./matching/tmp/configure.in
./matching/tmp/configure
./matching/tmp/config.cache
./matching/tmp/config.log
./matching/tmp/config.status
./matching/tmp/Makefile.in
./matching/tmp/Makefile.am
./matching/tmp/ChangeLog
./matching/tmp/NEWS
./matching/tmp/BitVector.h
./matching/main.cc
./matching/BitVector.h,v
./matching/datastructures.cc
./matching/forwarding8_5.tgz
./matching/farwarding.h
./net
./net/projects
./net/projects/CVS
./net/projects/CVS/Root
./net/projects/CVS/Repository
./net/projects/CVS/Entries
./net/projects/CVS/Entries.Log
./net/projects/CVS/Entries.Static
./net/projects/siena.net
./net/projects/siena.net/CVS
./net/projects/siena.net/CVS/Root
./net/projects/siena.net/CVS/Repository
./net/projects/siena.net/CVS/Entries
./net/projects/siena.net/CVS/Entries.Log
./net/projects/siena.net/CVS/Entries.Static
./net/projects/siena.net/src
./net/projects/siena.net/src/CVS
./net/projects/siena.net/src/CVS/Root
./net/projects/siena.net/src/CVS/Repository
./net/projects/siena.net/src/CVS/Entries
./net/projects/siena.net/src/CVS/Entries.Log
./net/projects/siena.net/src/CVS/Entries.Static
./net/projects/siena.net/src/edu
./net/projects/siena.net/src/edu/CVS
./net/projects/siena.net/src/edu/CVS/Root
./net/projects/siena.net/src/edu/CVS/Repository
./net/projects/siena.net/src/edu/CVS/Entries
./net/projects/siena.net/src/edu/CVS/Entries.Log
./net/projects/siena.net/src/edu/CVS/Entries.Static
./net/projects/siena.net/src/edu/colorado
./net/projects/siena.net/src/edu/colorado/CVS
./net/projects/siena.net/src/edu/colorado/CVS/Root
./net/projects/siena.net/src/edu/colorado/CVS/Repository
./net/projects/siena.net/src/edu/colorado/CVS/Entries
./net/projects/siena.net/src/edu/colorado/CVS/Entries.Log
./net/projects/siena.net/src/edu/colorado/CVS/Entries.Static
./net/projects/siena.net/src/edu/colorado/serl
./net/projects/siena.net/src/edu/colorado/serl/CVS
./net/projects/siena.net/src/edu/colorado/serl/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/CVS/Entries.Log
./net/projects/siena.net/src/edu/colorado/serl/CVS/Entries.Static
./net/projects/siena.net/src/edu/colorado/serl/siena
./net/projects/siena.net/src/edu/colorado/serl/siena/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/CVS/Entries.Log
./net/projects/siena.net/src/edu/colorado/serl/siena/CVS/Entries.Static
./net/projects/siena.net/src/edu/colorado/serl/siena/net
./net/projects/siena.net/src/edu/colorado/serl/siena/net/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/net/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/net/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/net/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/net/CVS/Entries.Log
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/CVS/Entries.Log
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/PacketSenderReceiver.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/SienaTest.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/test/SienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/PacketReceiverFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/PacketSenderFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/SienaURI.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/SienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/TCPPacketReceiver.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/TCPPacketSender.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/UDPPacketReceiver.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/senp/UDPPacketSender.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/CVS/Entries.Log
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/PacketReceiverPerf.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/PacketSenderPerf.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/PacketSenderReceiver.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/PacketSenderReceiverPerf.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/SienaTest.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/test/SienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/MessageCode.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketReceiver.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketReceiverAccept.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketReceiverFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketReceiverSocket.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketReceiverThread.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketSender.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/PacketSenderFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/SienaURI.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/tcp2/SienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/ActiveException.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/InvalidReceiverException.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/PacketReceiverFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/PacketSenderFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/SerlPacketReceiverFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/SerlPacketSenderFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/SerlSienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/ShutdownException.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/SienaURI.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/SienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/UnknownProtocolException.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/CVS
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/CVS/Root
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/CVS/Repository
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/CVS/Entries
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/PacketReceiverPerf.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/PacketSenderPerf.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/PacketSenderReceiver.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/PacketSenderReceiverPerf.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/SerlSienaTest.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/SerlSienaURIFactory.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/SienaTest.java
./net/projects/siena.net/src/edu/colorado/serl/siena/net/test/SienaURIFactory.java
./net/projects/util
./net/projects/util/CVS
./net/projects/util/CVS/Root
./net/projects/util/CVS/Repository
./net/projects/util/CVS/Entries
./net/projects/util/CVS/Entries.Log
./net/projects/util/CVS/Entries.Static
./net/projects/util/src
./net/projects/util/src/CVS
./net/projects/util/src/CVS/Root
./net/projects/util/src/CVS/Repository
./net/projects/util/src/CVS/Entries
./net/projects/util/src/CVS/Entries.Log
./net/projects/util/src/CVS/Entries.Static
./net/projects/util/src/edu
./net/projects/util/src/edu/CVS
./net/projects/util/src/edu/CVS/Root
./net/projects/util/src/edu/CVS/Repository
./net/projects/util/src/edu/CVS/Entries
./net/projects/util/src/edu/CVS/Entries.Log
./net/projects/util/src/edu/CVS/Entries.Static
./net/projects/util/src/edu/colorado
./net/projects/util/src/edu/colorado/CVS
./net/projects/util/src/edu/colorado/CVS/Root
./net/projects/util/src/edu/colorado/CVS/Repository
./net/projects/util/src/edu/colorado/CVS/Entries
./net/projects/util/src/edu/colorado/CVS/Entries.Log
./net/projects/util/src/edu/colorado/CVS/Entries.Static
./net/projects/util/src/edu/colorado/serl
./net/projects/util/src/edu/colorado/serl/CVS
./net/projects/util/src/edu/colorado/serl/CVS/Root
./net/projects/util/src/edu/colorado/serl/CVS/Repository
./net/projects/util/src/edu/colorado/serl/CVS/Entries
./net/projects/util/src/edu/colorado/serl/CVS/Entries.Log
./net/projects/util/src/edu/colorado/serl/CVS/Entries.Static
./net/projects/util/src/edu/colorado/serl/util
./net/projects/util/src/edu/colorado/serl/util/CVS
./net/projects/util/src/edu/colorado/serl/util/CVS/Root
./net/projects/util/src/edu/colorado/serl/util/CVS/Repository
./net/projects/util/src/edu/colorado/serl/util/CVS/Entries
./net/projects/util/src/edu/colorado/serl/util/CVS/Entries.Log
./net/projects/util/src/edu/colorado/serl/util/synch
./net/projects/util/src/edu/colorado/serl/util/synch/CVS
./net/projects/util/src/edu/colorado/serl/util/synch/CVS/Root
./net/projects/util/src/edu/colorado/serl/util/synch/CVS/Repository
./net/projects/util/src/edu/colorado/serl/util/synch/CVS/Entries
./net/projects/util/src/edu/colorado/serl/util/synch/Mutex.java
./net/projects/util/src/edu/colorado/serl/util/synch/OwnerException.java
./net/projects/util/src/edu/colorado/serl/util/synch/TimedOutException.java
./net/projects/util/src/edu/colorado/serl/util/thread
./net/projects/util/src/edu/colorado/serl/util/thread/CVS
./net/projects/util/src/edu/colorado/serl/util/thread/CVS/Root
./net/projects/util/src/edu/colorado/serl/util/thread/CVS/Repository
./net/projects/util/src/edu/colorado/serl/util/thread/CVS/Entries
./net/projects/util/src/edu/colorado/serl/util/thread/Maintainable.java
./net/projects/util/src/edu/colorado/serl/util/thread/MaintenanceThread.java
./serl.tgz
./router
./router/aclocal.m4
./router/configure.in
./router/Makefile.in
./router/VERSION
./router/INSTALL
./router/LICENSE
./router/install-sh
./router/mkinstalldirs
./router/sienaconf.in
./router/RCS
./router/README
./router/srconf.in
./router/configure
./router/selector.h
./siena.net.tgz
./siena_net
./siena_net/.SienaURI.java.swp
./siena_net/config.cache
./siena_net/config.log
./siena_net/config.status
./siena_net/configure
./siena_net/configure.in
./siena_net/ConnectionFlags.java
./siena_net/GenericPacketSenderFactory.java
./siena_net/Makefile
./siena_net/Makefile.vars
./siena_net/Makefile.vars.in
./siena_net/NoSuchFlagException.java
./siena_net/SienaURI.java
./siena_net/TCPPacketReceiver.java
./siena_net/TCPPacketSender.java
./siena_net/tests
./siena_net/tests/.TestSienaURI.java.swp
./siena_net/tests/Makefile
./siena_net/tests/Test.class
./siena_net/tests/Test.java
./siena_net/tests/TestSienaURI.java
./siena_net/tests/TestTCPFlagsCase.java
./siena_net/tests/TestTCPPacketSenderReceiver.java
./siena_net/tests/TestTCPReceiverFlags.java
./siena_net/tests/TestTCPSenderFlags.java
./siena_net/tests/TestTCPVersion.java
./siena_net/tests/TestThinClient.java
./siena_net/tests/TestUDPPacketSenderReceiver.java
./siena_net/UDPPacketReceiver.java
./siena_net/UDPPacketSender.java
./siena_net/UnknownProtocolException.java
./counting
./counting/Makefile
./counting/Makefile.in
./counting/MatchedMaps.cc
./counting/MatchedMaps.h
./counting/Siena.h
./counting/VERSION
./counting/attribute_n
./counting/config.cache
./counting/config.log
./counting/config.status
./counting/configure
./counting/configure.in
./counting/configure.scan
./counting/constraints_map.cc
./counting/constraints_map.temp.cc
./counting/event1
./counting/exp.cfg
./counting/exp.cfg.bak
./counting/experiment.cc
./counting/filter_records.cc
./counting/filters3.1
./counting/filters6
./counting/filters7
./counting/filters8
./counting/filters9
./counting/forwarding.cc
./counting/forwarding.h
./counting/forwarding_table.cc
./counting/gen_filters.cc
./counting/gen_notifications.cc
./counting/interface.cc
./counting/.struct.cc.swp
./counting/matching.cc
./counting/matching.h
./counting/matching.temp.cc
./counting/new_dic.4
./counting/nfile1
./counting/CVS
./counting/CVS/Root
./counting/CVS/Repository
./counting/CVS/Entries
./counting/notification.cc
./counting/orig.h
./counting/.#interface.cc.1.1
./counting/operators
./counting/predicate.h
./counting/result
./counting/struct.cc
./counting/struct.h
./counting/struct.temp.h
./counting/structure.h
./counting/structure8_22.cc
./counting/structure8_22.h
./counting/types
./counting/experiments
./counting/experiments/operators
./counting/experiments/run_scenario.sh.in
./counting/experiments/scenario1
./counting/experiments/run_scenario.sh
./CVS
./CVS/Root
./CVS/Repository
./CVS/Entries
./CVSROOT
./CVSROOT/CVS
./CVSROOT/CVS/Root
./CVSROOT/CVS/Repository
./CVSROOT/CVS/Entries
./CVSROOT/checkoutlist
./CVSROOT/commitinfo
./CVSROOT/config
./CVSROOT/cvswrappers
./CVSROOT/editinfo
./CVSROOT/loginfo
./CVSROOT/modules
./CVSROOT/notify
./CVSROOT/rcsinfo
./CVSROOT/taginfo
./CVSROOT/verifymsg
./RCS
./forwarding
./forwarding/configure.in
./forwarding/Makefile.in
./forwarding/tests
./forwarding/tests/CVS
./forwarding/tests/CVS/Root
./forwarding/tests/CVS/Repository
./forwarding/tests/CVS/Entries
EOF
cat test.in1 test.in1 test.in1 > test.in
rm -f test.in1
#
# performance test 
#
# echo testing KA with slow senders... 
# test_performance -ka 5 2 2 200
# test_performance -ka 4 2 2 200
# test_performance -ka 3 2 2 200
# test_performance -ka 2 2 2 200
# test_performance -ka 1 2 2 200
# #

#
# testing KA... 
#
test_performance ka 16 16
test_performance ka 8 16
test_performance ka 4 16
test_performance ka 2 16
test_performance ka 1 16
#
test_performance ka 16 8
test_performance ka 8 8
test_performance ka 4 8
test_performance ka 2 8
test_performance ka 1 8
#
test_performance ka 16 4
test_performance ka 8 4
test_performance ka 4 4
test_performance ka 2 4
test_performance ka 1 4
#
test_performance ka 16 2
test_performance ka 8 2
test_performance ka 4 2
test_performance ka 2 2
test_performance ka 1 2
#
test_performance ka 16 1
test_performance ka 8 1
test_performance ka 4 1
test_performance ka 2 1
test_performance ka 1 1
#
# testing TCP... 
#
test_performance tcp 16 16
test_performance tcp 8 16
test_performance tcp 4 16
test_performance tcp 2 16
test_performance tcp 1 16
#
test_performance tcp 16 8
test_performance tcp 8 8
test_performance tcp 4 8
test_performance tcp 2 8
test_performance tcp 1 8
#
test_performance tcp 16 4
test_performance tcp 8 4
test_performance tcp 4 4
test_performance tcp 2 4
test_performance tcp 1 4
#
test_performance tcp 16 2
test_performance tcp 8 2
test_performance tcp 4 2
test_performance tcp 2 2
test_performance tcp 1 2
#
test_performance tcp 16 1
test_performance tcp 8 1
test_performance tcp 4 1
test_performance tcp 2 1
test_performance tcp 1 1
#
# testing UDP... 
#
test_performance udp 16 16
test_performance udp 8 16
test_performance udp 4 16
test_performance udp 2 16
test_performance udp 1 16
#
test_performance udp 16 8
test_performance udp 8 8
test_performance udp 4 8
test_performance udp 2 8
test_performance udp 1 8
#
test_performance udp 16 4
test_performance udp 8 4
test_performance udp 4 4
test_performance udp 2 4
test_performance udp 1 4
#
test_performance udp 16 2
test_performance udp 8 2
test_performance udp 4 2
test_performance udp 2 2
test_performance udp 1 2
#
test_performance udp 16 1
test_performance udp 8 1
test_performance udp 4 1
test_performance udp 2 1
test_performance udp 1 1
#
# testing SSL... 
#
test_performance ssl 16 16
test_performance ssl 8 16
test_performance ssl 4 16
test_performance ssl 2 16
test_performance ssl 1 16
#
test_performance ssl 16 8
test_performance ssl 8 8
test_performance ssl 4 8
test_performance ssl 2 8
test_performance ssl 1 8
#
test_performance ssl 16 4
test_performance ssl 8 4
test_performance ssl 4 4
test_performance ssl 2 4
test_performance ssl 1 4
#
test_performance ssl 16 2
test_performance ssl 8 2
test_performance ssl 4 2
test_performance ssl 2 2
test_performance ssl 1 2
#
test_performance ssl 16 1
test_performance ssl 8 1
test_performance ssl 4 1
test_performance ssl 2 1
test_performance ssl 1 1
