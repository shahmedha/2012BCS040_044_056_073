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


top_srcdir	= ..
srcdir		= .
top_builddir	= ..
prefix		= /opt
javadir		= $(prefix)/java



JAVA		= /usr/bin/java
JAVAC		= /usr/bin/javac
JAVADOC		= /usr/bin/javadoc
JAR		= /usr/bin/jar

PACKAGE_NAME	= siena
PACKAGE_VERSION	= 2.0.4

SIENAJAR	= $(top_builddir)/$(PACKAGE_NAME)-$(PACKAGE_VERSION).jar
CLASSPATH	= :$(SIENAJAR)

CLASSDIR	= .
JDEBUGFLAGS	= -Xlint:unchecked
JFLAGS		= $(JDEBUGFLAGS) -O -d $(CLASSDIR) -classpath $(CLASSPATH)

#
# these are the source files for all the original Siena tests.  They
# are now grouped in the sienatest directory, whether they are in the
# sienatest package or not.  This is because we are now structuring
# the source files in this test directory by putting all JUnit tests
# in a source tree that mirrors the structure of the main source tree.
#
SIENATEST_SOURCES = sienatest/Compare.java sienatest/TestFilter.java	\
	sienatest/ReceiverFactory.java sienatest/InterestedParty.java	\
	sienatest/ObjectOfInterest.java					\
	sienatest/TestNotificationBuffer.java				\
	sienatest/PatternReader.java					\
	sienatest/TestPacketReceiver.java				\
	sienatest/TestPacketSender.java sienatest/TestPatterns2.java	\
	sienatest/TestPatterns.java					\
	sienatest/TestReceiverPerformance.java				\
	sienatest/TestBase.java sienatest/TestReceiverShutdown.java	\
	sienatest/TestSenderPerformance.java				\
	sienatest/TestCovering.java sienatest/TestShutdown.java		\
	sienatest/TestDecode.java sienatest/TestEncode.java		\
	sienatest/TestThinClient.java					\
	sienatest/TestEncodePerformance.java				\
	sienatest/TestTokenizer.java					\
	sienatest/TestNotificationBuffer2.java				\
	sienatest/TestNotificationSequencer.java			\
	sienatest/MultiSender.java sienatest/SequencerSubscriber.java	\
	sienatest/TestLocalPubSub.java sienatest/Receiver.java		\
	sienatest/Sender.java sienatest/TestClearSubscriptions.java

EXAMPLES = sienatest/InterestedParty.java sienatest/ObjectOfInterest.java

# Siena Original Tests:
#
# these are the tests from the original Siena package.  Some of these
# tests are now failing due to the new DV/DRP code.  This is mostly
# due to reconfiguration of the test environment and to the fact that
# all tests are based on HierarchicalDispatcher which is no longer
# functional in this implementation.  The following macros are defined
# to keep track of all the original tests, even though we end up
# ignoring all the failing ones.
# 
SIENA_FAILING_TESTS = \
	st_storage st_local_pubsub st_remote_sub \
	st_patterns st_patterns2 st_remote_patterns st_notif_buffer \
	st_notif_sequencer st_remote_sequencer st_clearsubs st_shutdown	\
	st_receiver_shutdown st_topology


SIENA_PASSING_TESTS = \
	st_tokenizer st_filter st_covering st_encode st_thinclient

SIENA_PERFORMANCE_TESTS = st_comm_performance
SIENA_EXTRA_TESTS =  st_packet_receiver

SIENA_TESTS = $(SIENA_PASSING_TESTS)

#
# JUnit and more recent tests
#
JUNIT_SOURCES = \
	siena/TestSFFCases.java \
	siena/TestDVDRPDispatcherRemote.java \
	siena/dvdrp/TestBloomFilter.java \
	siena/dvdrp/TestOrderedByteArrayMap.java \
	siena/dvdrp/TestDVEntry.java \
	siena/dvdrp/AllTests.java \
	siena/dvdrp/TestHash.java \
	siena/TestSENPEncodeDecode.java \
	siena/TestHierarchicalDispatcherRemote.java \
	siena/TestDVDRPDispatcher.java \
	siena/SingleNotifiableClient.java

JUNIT_TESTS = \
	siena.TestSFFCases \
	siena.TestDVDRPDispatcherRemote \
	siena.dvdrp.TestBloomFilter \
	siena.dvdrp.TestOrderedByteArrayMap \
	siena.dvdrp.TestDVEntry \
	siena.dvdrp.TestHash \
	siena.TestSENPEncodeDecode \
	siena.TestHierarchicalDispatcherRemote \
	siena.TestDVDRPDispatcher

.PHONY: all compileall clean veryclean distclean maintainerclean \
	basictests performancetests junit examples

#
# these are the (phony) target that run our tests
#
all: examples $(SIENA_TESTS) junit

#
# these are the (phony) targets to compile the various test sources
#
.sienatest.javac.done: $(SIENATEST_SOURCES)
	$(JAVAC) $(JFLAGS) $^
	touch $@

.junit.javac.done: $(JUNIT_SOURCES)
	$(JAVAC) $(JFLAGS) $^
	touch $@

examples: $(EXAMPLES)
	$(JAVAC) $(JFLAGS) $^

#
# and these are the (phony) targets for each individual test
#
st_%:: .sienatest.javac.done config.sh $(srcdir)/test_%.sh
	@$(srcdir)/test_$*.sh

junit: .junit.javac.done
	@$(JAVA) -cp $(CLASSPATH):. org.junit.runner.JUnitCore \
		$(JUNIT_TESTS) | tee .junit_tests.log

jut_%:: .junit.javac.done
	@$(JAVA) -cp $(CLASSPATH):. org.junit.runner.JUnitCore $* \
	 | tee .junit_tests.log

#
# for some reason this doesn't work... oh well
#
#_%: $(srcdir)/test_%.sh .javac.done config.sh
#	$(srcdir)/test_$*.sh
#
clean:
	/bin/rm -rf test.* *.log *.info *. *.pid* raddr \
		server*.log server*.store raddr \
		.junit_tests.log .junit.javac.done .sienatest.javac.done

veryclean: clean
	find . -name \*.class -print0 | xargs -0 rm -f

distclean: clean
	/bin/rm -f *~ Makefile config.sh config.status config.cache \
		*.log *.info server.pids 

maintainerclean: distclean
#
# autoconf stuff
#
$(top_srcdir)/configure: $(top_srcdir)/configure.ac
	cd $(top_srcdir) && autoconf

$(top_builddir)/config.status: $(top_srcdir)/configure
	cd $(top_builddir) && $(SHELL) ./config.status --recheck

Makefile: $(srcdir)/Makefile.in $(top_builddir)/config.status
	cd .. && $(SHELL) ./config.status test/$@

config.sh: $(srcdir)/config.sh.in $(top_builddir)/config.status
	cd .. && $(SHELL) ./config.status test/$@
