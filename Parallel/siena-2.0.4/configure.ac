dnl
dnl  This file is part of Siena, a wide-area event notification system.
dnl  See http://www.inf.usi.ch/carzaniga/siena/
dnl
dnl  Author: Antonio Carzaniga (firstname.lastname@usi.ch)
dnl  See the file AUTHORS for full details. 
dnl
dnl  Copyright (C) 2011 Antonio Carzaniga
dnl
dnl
dnl  Siena is free software: you can redistribute it and/or modify
dnl  it under the terms of the GNU General Public License as published by
dnl  the Free Software Foundation, either version 3 of the License, or
dnl  (at your option) any later version.
dnl  
dnl  Siena is distributed in the hope that it will be useful,
dnl  but WITHOUT ANY WARRANTY; without even the implied warranty of
dnl  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
dnl  GNU General Public License for more details.
dnl  
dnl  You should have received a copy of the GNU General Public License
dnl  along with Siena.  If not, see <http://www.gnu.org/licenses/>.
dnl
AC_INIT(siena, 2.0.4, [Antonio Carzaniga (firstname.lastname@usi.ch)])
AC_PREFIX_DEFAULT(/opt)

AC_PROG_MAKE_SET
dnl AC_PROG_INSTALL
dnl
dnl  Java stuff
dnl
AC_ARG_WITH(javabin, [  --with-javabin=dir  finds java executables in dir], PATH="$withval:$PATH")
dnl 
AC_ARG_WITH(classpath, [  --with-classpath=classpath  finds java libraries], CLASSPATH="$withval")
AC_SUBST(CLASSPATH)
dnl 
AC_PATH_PROGS(JAVAC, javac)
test -z "$JAVAC" && AC_MSG_ERROR([
No acceptable Java compiler found in \$PATH
You may specify a Java compiler by setting the env variable JAVAC
or you might use --with-javabin to specify the java binary directory])
dnl
AC_PATH_PROGS(JAVA,java)
test -z "$JAVA" && AC_MSG_ERROR([
No acceptable Java VM program found in \$PATH
You may specify a Java VM by setting the env variable JAVA
or you might use --with-javabin to specify the java binary directory])
dnl
AC_PATH_PROGS(JAVADOC,javadoc)
test -z "$JAVADOC" && AC_MSG_ERROR([
No acceptable JavaDoc program found in \$PATH
You may specify a JavaDoc by setting the env variable JAVADOC
or you might use --with-javabin to specify the java binary directory])
dnl
AC_PATH_PROGS(JAR,jar) 
test -z "$JAR" && AC_MSG_ERROR([
No acceptable Jar program found in \$PATH
You may specify a Jar by setting the env variable JAR
or you might use --with-javabin to specify the java binary directory])
dnl
AC_SUBST(JAVA)
AC_SUBST(JAVAC)
AC_SUBST(JAVADOC)
AC_SUBST(JAR)
AC_SUBST(CLASSPATH)
AC_OUTPUT(Makefile test/Makefile test/config.sh)
