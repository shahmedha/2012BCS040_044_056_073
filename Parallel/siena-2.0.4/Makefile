#
#  This file is part of Siena, a wide-area event notification system.
#  See http://www.inf.usi.ch/carzaniga/siena/
#
#  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
#  See the file AUTHORS for full details. 
#
#  Copyright (C) 1998-2002 University of Colorado
#  Copyright (C) 2011 Antonio Carzaniga
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
SHELL		= /bin/sh
top_srcdir	= .
srcdir		= .

java_src	= $(srcdir)/src

VPATH		= $(top_srcdir)

PACKAGE_NAME	= siena
PACKAGE_VERSION	= 2.0.4
PACKAGE_TARNAME	= siena


prefix		= /opt
docdir		= ${datarootdir}/doc/${PACKAGE_TARNAME}
datarootdir	= ${prefix}/share
javadir		= $(prefix)/java



JAVAC		= /usr/bin/javac
JAVADOC		= /usr/bin/javadoc
JAR		= /usr/bin/jar

PACKAGE		= $(PACKAGE_NAME)-$(PACKAGE_VERSION)
SRC_PACKAGE	= $(PACKAGE_NAME)-java-$(PACKAGE_VERSION)
DOC_PACKAGE	= $(PACKAGE_NAME)-javadoc-$(PACKAGE_VERSION)

CLASSDIR	= classes
HTML_DOCDIR	= doc/html

CLASSPATH	= 
JDEBUGFLAGS	= -O -Xlint
JFLAGS		= $(JDEBUGFLAGS) -sourcepath $(java_src) -d $(CLASSDIR) -classpath "$(CLASSPATH)"
JDOCFLAGS	= -sourcepath $(java_src) \
		  -doctitle 'Siena $(PACKAGE_VERSION) API Documentation' \
		  -windowtitle 'Siena $(PACKAGE_VERSION) API Documentation' \
		  -bottom '<div align=right><small>\
		  Copyright &\#169; 2000-2005  University of Colorado.<br>\
		  Copyright &\#169; 2005-2011  Antonio Carzaniga.<br>\
		  This documentation is maintained by <a href="http://www.inf.usi.ch/carzaniga/">Antonio Carzaniga</a></small></div>' \
		  -header '<a href="http://www.inf.usi.ch/carzaniga/siena/">Siena</a> (v.$(PACKAGE_VERSION)) API documentation' \
		  -nodeprecatedlist \
		  -overview $(top_srcdir)/doc/overview.html \
		  -stylesheetfile $(top_srcdir)/doc/stylesheet.css

DOCOVERVIEWS =	doc/overview.html \
		src/siena/package.html src/siena/comm/package.html
DOC_FILES =	AUTHORS LICENSE README INSTALL INSTALL.ant \
		doc/stylesheet.css \
		doc/fdl.txt		\
		$(DOCOVERVIEWS)
DOCPACKAGES =	siena siena.comm siena.fwd siena.dvdrp

BUILD_FILES	= Makefile.in configure configure.ac build.xml

SOURCES = \
	src/siena/Siena.java					\
	src/siena/AttributeValue.java				\
	src/siena/SienaException.java				\
	src/siena/TwoPrioritiesPacketQueue.java			\
	src/siena/NotificationInputSequencer.java		\
	src/siena/Notification.java				\
	src/siena/AttributeConstraint.java			\
	src/siena/Pattern.java					\
	src/siena/DirectSENPInterface.java			\
	src/siena/DVHeartbeat.java				\
	src/siena/HierarchicalDispatcher.java			\
	src/siena/DispatcherThread.java				\
	src/siena/comm/PacketSender.java			\
	src/siena/comm/GenericReceiverFactory.java		\
	src/siena/comm/PacketReceiverFactory.java		\
	src/siena/comm/UDPPacketReceiver.java			\
	src/siena/comm/TCPPacketSender.java			\
	src/siena/comm/PacketReceiverFatalError.java		\
	src/siena/comm/MultiPacketReceiver.java			\
	src/siena/comm/InvalidSenderException.java		\
	src/siena/comm/SSLPacketReceiver.java			\
	src/siena/comm/InvalidReceiverException.java		\
	src/siena/comm/PacketReceiver.java			\
	src/siena/comm/InetPacketReceiver.java			\
	src/siena/comm/PacketReceiverClosed.java		\
	src/siena/comm/PacketSenderFactory.java			\
	src/siena/comm/KAZipPacketReceiver.java			\
	src/siena/comm/TimeoutExpired.java			\
	src/siena/comm/KAPacketSender.java			\
	src/siena/comm/KAZipPacketSender.java			\
	src/siena/comm/PacketReceiverException.java		\
	src/siena/comm/TCPPacketReceiver.java			\
	src/siena/comm/SSLPacketSender.java			\
	src/siena/comm/PacketSenderException.java		\
	src/siena/comm/InetPacketSender.java			\
	src/siena/comm/KAPacketReceiver.java			\
	src/siena/comm/GenericSenderFactory.java		\
	src/siena/comm/UDPPacketSender.java			\
	src/siena/SienaId.java					\
	src/siena/FilterList.java				\
	src/siena/Monitor.java					\
	src/siena/dvdrp/TwoPrioritiesListQueue.java		\
	src/siena/dvdrp/PredicatesTableEntry.java		\
	src/siena/dvdrp/DistanceVector.java			\
	src/siena/dvdrp/SlowArrayQueue.java			\
	src/siena/dvdrp/Buffer.java				\
	src/siena/dvdrp/AbstractTwoPrioritiesBufferQueue.java	\
	src/siena/dvdrp/ByteArrayComparator.java		\
	src/siena/dvdrp/AbstractBufferQueue.java		\
	src/siena/dvdrp/TwoPrioritiesQueue.java			\
	src/siena/dvdrp/BufferQueue.java			\
	src/siena/dvdrp/TwoPrioritiesBufferQueue.java		\
	src/siena/dvdrp/PacketQueue.java			\
	src/siena/dvdrp/QueueFullException.java			\
	src/siena/dvdrp/FastArrayQueue.java			\
	src/siena/dvdrp/PredicatesTable.java			\
	src/siena/dvdrp/Hash.java				\
	src/siena/dvdrp/BSet.java				\
	src/siena/dvdrp/DVEntry.java				\
	src/siena/dvdrp/StaticBufferQueue.java			\
	src/siena/dvdrp/ArrayQueue.java				\
	src/siena/dvdrp/OrderedByteArrayMap.java		\
	src/siena/dvdrp/BSetBV.java				\
	src/siena/dvdrp/BSetNode.java				\
	src/siena/dvdrp/ReceiverThread.java			\
	src/siena/dvdrp/TwoPrioritiesStaticBufferQueue.java	\
	src/siena/dvdrp/Base64.java				\
	src/siena/dvdrp/PoolAndQueue.java			\
	src/siena/dvdrp/TwoPrioritiesArrayQueue.java		\
	src/siena/dvdrp/BSetTable.java				\
	src/siena/dvdrp/OrderedByteArraySet.java		\
	src/siena/dvdrp/BufferQueueImpl.java			\
	src/siena/SENP.java					\
	src/siena/Op.java					\
	src/siena/Notifiable.java				\
	src/siena/Logging.java					\
	src/siena/Interface.java				\
	src/siena/fwd/Message.java				\
	src/siena/fwd/BadConstraintException.java		\
	src/siena/fwd/Value.java				\
	src/siena/fwd/Attribute.java				\
	src/siena/fwd/Constraint.java				\
	src/siena/fwd/MatchHandler.java				\
	src/siena/fwd/MatchMessageHandler.java			\
	src/siena/fwd/SFFTable.java				\
	src/siena/fwd/Predicate.java				\
	src/siena/fwd/ForwardingTable.java			\
	src/siena/fwd/Filter.java				\
	src/siena/DiscoveryManager.java				\
	src/siena/NotificationOutputSequencer.java		\
	src/siena/StartDVDRPServer.java				\
	src/siena/Covering.java					\
	src/siena/DVDRPDispatcher.java				\
	src/siena/DVDRPControl.java				\
	src/siena/NotificationBuffer.java			\
	src/siena/StartServer.java				\
	src/siena/SENPInvalidFormat.java			\
	src/siena/ThinClient.java				\
	src/siena/SFFHeartbeat.java				\
	src/siena/Filter.java 

TEST_FILES = test/Makefile.in test/config.sh.in test/static_config.sh   \
	test/sienakeystore.jks						\
	test/sienatest/Compare.java test/sienatest/TestFilter.java	\
	test/sienatest/ReceiverFactory.java				\
	test/sienatest/InterestedParty.java				\
	test/sienatest/ObjectOfInterest.java				\
	test/sienatest/TestNotificationBuffer.java			\
	test/sienatest/PatternReader.java				\
	test/sienatest/TestPacketReceiver.java				\
	test/sienatest/TestPacketSender.java				\
	test/sienatest/TestPatterns2.java				\
	test/sienatest/TestPatterns.java				\
	test/sienatest/TestReceiverPerformance.java			\
	test/sienatest/TestBase.java					\
	test/sienatest/TestReceiverShutdown.java			\
	test/sienatest/TestSenderPerformance.java			\
	test/sienatest/TestCovering.java				\
	test/sienatest/TestShutdown.java				\
	test/sienatest/TestDecode.java test/sienatest/TestEncode.java	\
	test/sienatest/TestThinClient.java				\
	test/sienatest/TestEncodePerformance.java			\
	test/sienatest/TestTokenizer.java				\
	test/sienatest/TestNotificationBuffer2.java			\
	test/sienatest/TestNotificationSequencer.java			\
	test/sienatest/MultiSender.java					\
	test/sienatest/SequencerSubscriber.java				\
	test/sienatest/TestLocalPubSub.java				\
	test/sienatest/Receiver.java test/sienatest/Sender.java		\
	test/sienatest/TestClearSubscriptions.java			\
									\
	test/sienatest/InterestedParty.java				\
	test/sienatest/ObjectOfInterest.java				\
									\
	test/test_storage.sh test/test_local_pubsub.sh			\
	test/test_remote_sub.sh test/test_thinclient.sh			\
	test/test_patterns.sh test/test_patterns2.sh			\
	test/test_remote_patterns.sh test/test_notif_buffer.sh		\
	test/test_notif_sequencer.sh test/test_remote_sequencer.sh	\
	test/test_clearsubs.sh test/test_shutdown.sh			\
	test/test_receiver_shutdown.sh test/test_tokenizer.sh		\
	test/test_filter.sh test/test_covering.sh test/test_encode.sh	\
	test/test_comm_performance.sh test/test_packet_receiver.sh	\
	test/test_topology.sh						\
	test/siena/TestSFFCases.java					\
	test/siena/TestDVDRPDispatcherRemote.java			\
	test/siena/dvdrp/TestBloomFilter.java				\
	test/siena/dvdrp/TestOrderedByteArrayMap.java			\
	test/siena/dvdrp/TestDVEntry.java				\
	test/siena/dvdrp/AllTests.java					\
	test/siena/dvdrp/TestHash.java					\
	test/siena/TestSENPEncodeDecode.java				\
	test/siena/TestHierarchicalDispatcherRemote.java		\
	test/siena/TestDVDRPDispatcher.java				\
	test/siena/SingleNotifiableClient.java

SRCDIST = $(DOC_FILES) $(BUILD_FILES) $(SOURCES) $(TEST_FILES)

default: binpkg

all: binpkg docpkg srcpkg

binpkg: $(PACKAGE).jar
srcpkg: $(SRC_PACKAGE).tar.gz
docpkg: $(DOC_PACKAGE).tar.gz
doc: $(HTML_DOCDIR)/index.html

.PHONY: all install clean distclean maintainerclean \
	testpackage test doc \
	srcpkg docpkg binpkg htmldoc

$(PACKAGE).jar: $(SOURCES)
	rm -rf $(CLASSDIR)
	mkdir $(CLASSDIR)
	$(JAVAC) $(JFLAGS) $^
	$(JAR) cvf $@ -C $(CLASSDIR) siena

DOCDATA = $(top_srcdir)/doc/stylesheet.css $(top_srcdir)/doc/fdl.txt 

$(HTML_DOCDIR)/index.html $(HTML_DOCDIR): $(DOC_FILES) $(SOURCES) $(DOCOVERVIEWS) $(DOCDATA)
	/bin/rm -rf $(HTML_DOCDIR)
	mkdir -p $(HTML_DOCDIR) 
	$(JAVADOC) $(JDOCFLAGS) -d $(HTML_DOCDIR) $(DOCPACKAGES)
	cp $(DOCDATA) $(HTML_DOCDIR)/

$(DOC_PACKAGE).tar.gz: $(HTML_DOCDIR)
	tar cvf - $(HTML_DOCDIR) | gzip > $@

install: $(PACKAGE).jar $(DOC_PACKAGE).tar.gz
	test -d $(javadir) || mkdir -p $(javadir)
	$(INSTALL) -m 644 $(PACKAGE).jar $(DOC_PACKAGE).tar.gz $(javadir)

$(SRC_PACKAGE).tar.gz: $(SRCDIST)
	rm -rf $(PACKAGE)
	mkdir $(PACKAGE)
	(cd $(top_srcdir);tar cf - $(SRCDIST))|(cd $(PACKAGE);tar xf -)
	tar cf - $(PACKAGE) | gzip > $@
	/bin/rm -rf $(PACKAGE)

#
# I stole this from Automake
#
distdir = _distcheck
am__remove_distdir = \
  { test ! -d $(distdir) \
    || { find $(distdir) -type d ! -perm -200 -exec chmod u+w {} ';' \
         && rm -fr $(distdir); }; }

#
# my simple distcheck
#
distcheck: $(SRC_PACKAGE).tar.gz
	$(am__remove_distdir)
	rm -rf $(distdir)
	mkdir $(distdir)
	mkdir $(distdir)/_build
	mkdir $(distdir)/_inst
	cd $(distdir) && gzip -dc ../$(SRC_PACKAGE).tar.gz | tar xf -
	chmod a-w $(distdir)
	chmod -R a-w $(distdir)/$(PACKAGE)
	chmod a+w $(distdir)/_build
	chmod a+w $(distdir)/_inst
	cd $(distdir) && distcheck_base=`pwd | sed -e 's,^[^:\\/]:[\\/],/,'` \
	&& cd _build \
	&& CLASSPATH="$(CLASSPATH)" "$$distcheck_base"/$(PACKAGE)/configure --prefix="$$distcheck_base"/_inst $(DISTCHECK_CONFIGURE_FLAGS) \
	&& $(MAKE) all test
	$(am__remove_distdir)
	@echo "$(SRC_PACKAGE).tar.gz is ready for distribution" | sed 'h;s/./=/g;p;x;p;x'

test: $(PACKAGE).jar
	cd test && $(MAKE) all

clean:
	-cd test && $(MAKE) clean
	/bin/rm -rf $(CLASSDIR) $(HTML_DOCDIR) \
		$(PACKAGE).jar $(DOC_PACKAGE).tar.gz $(SRC_PACKAGE).tar.gz

distclean: clean
	-cd tests && $(MAKE) distclean
	/bin/rm -f *~ Makefile config.status config.cache config.log \
		$(PACKAGE).* $(DOC_PACKAGE).* $(SRC_PACKAGE).*
	-rmdir tests
#
# maintainer stuff
#
maintainerclean: distclean

checkout:
	-cd $(top_srcdir) && co $(COFLAGS) $(SRCDIST) < /dev/null

#
# autoconf stuff
#
$(top_srcdir)/configure: $(top_srcdir)/configure.ac 
	cd $(top_srcdir) && autoconf

config.status: $(top_srcdir)/configure
	$(SHELL) ./config.status --recheck

Makefile: $(top_srcdir)/Makefile.in config.status
	$(SHELL) ./config.status $@

