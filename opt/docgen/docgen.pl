#############################################################################
# Project:     openmdx, http://www.openmdx.org/
# Name:        $Id: docgen.pl,v 1.3 2004/02/10 15:59:05 jlang Exp $
# Description: Docbook doc generation preprocessing script
# Revision:    $Revision: 1.3 $
# Owner:       OMEX AG, Switzerland, http://www.omex.ch
# Date:        $Date: 2004/02/10 15:59:05 $
#############################################################################
#
# This software is published under the BSD license
# as listed below.
# 
# Copyright (c) 2004, OMEX AG, Switzerland
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without 
# modification, are permitted provided that the following conditions 
# are met:
# 
# Redistribution and use in source and binary forms, with or
# without modification, are permitted provided that the following
# conditions are met:
# 
# * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
# 
# * Redistributions in binary form must reproduce the above copyright
# notice, this list of conditions and the following disclaimer in
# the documentation and/or other materials provided with the
# distribution.
# 
# * Neither the name of the openMDX team nor the names of its
# contributors may be used to endorse or promote products derived
# from this software without specific prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
# CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
# BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
# TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
# ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
# OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# 
# ------------------
# 
# This product includes software developed by the Apache Software
# Foundation (http://www.apache.org/).
#############################################################################

#############################################################################
#
# TODO   Check the delimiter for platform independence when setting the
#        environment variables 'SGML_CATALOG_FILES' and 'SGML_SEARCH_PATH'
#
#
#############################################################################


use Getopt::Std;

use File::Basename;
use File::Copy;
use File::Compare;
use Time::localtime;
use Time::tm;
use File::Find;


my $prog    = basename($0,"\.[^.]*");
my $VERSION = "1.1";

# Parse command line options
getopts('df:hi:o:qv', \%opt) || usage();

my $inFormat = $opt{'f'};
my $inFile   = $opt{'i'};
my $outDir   = $opt{'o'};
my $debug    = $opt{'d'};
my $quiet    = $opt{'q'};
my $version  = $opt{'v'};
my $help     = $opt{'h'};
my $retVal   = -1;



# Print version and exit
if ($version) {
  print "Version: $VERSION\n";
  exit 0;
}

# Print usage and exit
usage() if ($help);

# Check mandatory command line parameters
usage() if ($inFormat eq "" || $inFile eq "");

# Default command line parameters
$outDir = "." if ($outDir eq "");


# Only pdf and html doc generation is supported so far
usage() if (!($inFormat eq "pdf" ||
              $inFormat eq "html" ||
              $inFormat eq "htmlsingle" ||
              $inFormat eq "rtf"));


my $outLog = $outDir . "/" . "docgen.log"; # needs to be global

# Parse input file elements
my ($inFileName,$inFileDir,$inFileSuffix) = fileparse($inFile, ( '.sgml', '.sgm' ));


# Check input file
if (! -f $inFile) {
  $ts = timestamp();
  print("$prog:ERR:$ts> Inputfile '$inFile' missing!\n");
  exit 1;
}

# Check output dir
if (! -d $outDir) {
  $ts = timestamp();
  print("$prog:ERR:$ts> Output directory '$outDir' does not exist!\n");
  exit 1;
}

# Check environment variables
if (! $ENV{'DOCGEN_HOME'} ) {
  $ts = timestamp();
  print("$prog:ERR:$ts> The environment variable 'DOCGEN_HOME' has not been set!\n");
  exit 1;
}

if ($inFormat eq "pdf") {
  if (! $ENV{'MIKTEX_HOME'} ) {
    $ts = timestamp();
    print("$prog:ERR:$ts> The environment variable 'MIKTEX_HOME' has not been set!\n");
    exit 1;
  }
}

# Set the environment variables needed by the document generation tools
$ENV{'SGML_CATALOG_FILES'} = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/dsssl/catalog;" .
                             "$ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/catalog;" .
                             "$ENV{'DOCGEN_HOME'}/docbook-dtd-3.1/docbook.cat";
$ENV{'SGML_SEARCH_PATH'}   = ".;$ENV{'DOCGEN_HOME'}/openjade-1.3.1";
$ENV{'JADE'}               = "$ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78";


unlink $outLog if (-f $outLog);

# Some debug logs
logDbg("Format            :  $inFormat");
logDbg("Input file        :  $inFile");
logDbg("Output dir        :  $outDir");

logDbg("Log (intermediate):  $outLog");
logDbg("Log               :  $outDir/$inFileName.log");;

logDbg("inFile name       :  $inFileName");
logDbg("inFile path       :  $inFileDir");;
logDbg("inFile suffix     :  $inFileSuffix");;

logDbg("DOCGEN_HOME       :  $ENV{'DOCGEN_HOME'}");
logDbg("SGML_CATALOG_FILES:  $ENV{'SGML_CATALOG_FILES'}");
logDbg("SGML_SEARCH_PATH  :  $ENV{'SGML_SEARCH_PATH'}");
logDbg("JADE              :  $ENV{'JADE'}");



# Check if index generation is needed
my $needIndex = fileGrep($inFile, "!ENTITY index SYSTEM \"Index.sgml\"");


# Call document generator
if ($inFormat eq "pdf") {
  generateIndex(0) if ($needIndex > 0);
  $retVal = sgml2pdf();
}elsif ($inFormat eq "html") {
  generateIndex(0) if ($needIndex > 0);
  $retVal = sgml2html();
}elsif ($inFormat eq "htmlsingle") {
  generateIndex(1) if ($needIndex > 0);
  $retVal = sgml2htmlSingle();
}elsif ($inFormat eq "rtf") {
  generateIndex(0) if ($needIndex > 0);
  $retVal = sgml2rtf();
}

if ($debug) {
  # Keep files in debug mode
  move("index.sgml", $outDir);
  move("HTML.index", $outDir);
}else{
  unlink("index.sgml");
  unlink("HTML.index");
}

# Rename the docgen.log file
move($outLog, $outDir . "/" . $inFileName . ".log");


# Return an apropriate exit code
if ($retVal == 0) {
  exit 0;
}else{
  $ts = timestamp();
  print("$prog:ERR:$ts> Document generation FAILED!\n");
  exit 1;
}



###############################################################################
# generateIndex
#
# Generates a SGML index file and leaves it in the current directory so
# that the following sgml generators can use the file.
#
# $isHtml  Set to 1 if a single html is to be generated. Set to 0 for all
#          other cases.
# return   -
###############################################################################
sub generateIndex($isSingleHtmlFile) {
  my ($isSingleHtmlFile) = @_;

  my $outLogTmp = $outDir . "/" . docgen . ".log.tmp";
  my $command   = "";

  logMsg("Generating index");

  unlink("index.sgml");
  unlink("HTML.sgml");

  if ($isSingleHtmlFile == 1) {
    $command = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/bin/openjade " .
               "-E 1000 " .
               "-t sgml -f $outLogTmp " .
               "-d $ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/html/docbook.dsl " .
               "-V html-index -i html -V nochunks -V rootchunk " .
               "-V %root-filename%=$inFileName -V %html-ext%=.html -V html-index $inFile";
  }else{
    $command = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/bin/openjade " .
               "-E 1000 " .
               "-t sgml -f $outLogTmp " .
               "-d $ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/html/docbook.dsl " .
               "-V html-index $inFile";
  }

  logHeader($outLog, "HTML index ($inFile)");
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "");
  unlink $outLogTmp;



  # Create a new index file
  logMsg("Creating new DocBook index file \"index.sgml\" ...");
  $command = "perl " .
             "$ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/bin/collateindex.pl " .
             "-N -o index.sgml -e $outLogTmp";
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "collateindex.pl:");
  unlink $outLogTmp;

  # Load index entries
  logMsg("Loading index entries into \"index.sgml\" ...");
  $command = "perl " .
             "$ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/bin/collateindex.pl " .
             "-o index.sgml -e $outLogTmp html.index";
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "collateindex.pl:");
  unlink $outLogTmp;
}


###############################################################################
# sgml2rtf
#
# Generates a RTF file from a SGML file
#
# return  (a scalar) 0 on success
###############################################################################
sub sgml2rtf {

  my $outFile   = $outDir . "/" . $inFileName . ".rtf";
  my $outLogTmp = $outDir . "/" . docgen . ".log.tmp";
  my $command   = "";
  my $start     = time();


  # cleanup
  unlink $outFile;

  logHeader($outLog, "SGML to RTF ($inFile) ");

  $command = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/bin/openjade " .
             "-E 1000 " .
             "-t rtf -V rtf-backend -f $outLogTmp " .
             "-d $ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/print/docbook.dsl " .
             "$inFile";
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "");
  unlink $outLogTmp;

  $elapsed = time() - $start;
  logMsg("SGML to RTF ($inFile) elapsed time: $elapsed sec");

  move($inFileName . ".rtf", $outFile);

  logErr("Output file '$outFile' not found") if (! -f $outFile);
  return (-f $outFile) ? 0 : 1;
}



###############################################################################
# sgml2htmlSingle
#
# Generates a single HTML file from a SGML file
#
# return  (a scalar) 0 on success
###############################################################################
sub sgml2htmlSingle {

  my $outFile   = $outDir . "/" . $inFileName . ".html";
  my $outLogTmp = $outDir . "/" . docgen . ".log.tmp";
  my $command   = "";
  my $start     = time();


  # cleanup
  unlink $outFile;

  logHeader($outLog, "SGML to HTML ($inFile) ");

  $command = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/bin/openjade " .
             "-E 1000 " .
             "-t sgml -V nochunks -V rootchunk " .
             "-V %root-filename%=$inFileName -V %html-ext%=.html " .
             "-f $outLogTmp " .
             "-d $ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/html/docbook.dsl $inFile";
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "");
  unlink $outLogTmp;

  $elapsed = time() - $start;
  logMsg("SGML to single HTML ($inFile) elapsed time: $elapsed sec");

  move($inFileName . ".html", $outFile);

  logErr("Output file '$outFile' not found") if (! -f $outFile);
  return (-f $outFile) ? 0 : 1;
}



###############################################################################
# sgml2html
#
# Generates a multiple HTML files from a SGML file
#
# return  (a scalar) 0 on success
###############################################################################
sub sgml2html {

  my $outFile   = $outDir . "/book1.htm";
  my $outLogTmp = $outDir . "/" . docgen . ".log.tmp";
  my $command   = "";
  my $start     = time();


  # cleanup
  unlink $outFile;

  logHeader($outLog, "SGML to HTML ($inFile) ");

  $command = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/bin/openjade -t sgml " .
             "-E 1000 " .
             "-f $outLogTmp " .
             "-d $ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/html/docbook.dsl $inFile";
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "");
  unlink $outLogTmp;

  $elapsed = time() - $start;
  logMsg("SGML to HTML ($inFile) elapsed time: $elapsed sec");

  # Move all generated HTML files to the out directory. There are five generated
  # filetypes:
  #   root file:      book1.htm
  #   appendix:       a[0-9][0-9]*\.htm
  #   chapter:        c[0-9][0-9]*\.htm
  #   section:        x[0-9][0-9]*\.htm
  #   bibliography:   b[0-9][0-9]*\.htm
  #   index:          i[0-9][0-9]*\.htm
  opendir(DIR, ".");
  foreach $direntry (readdir(DIR)) {
    next if (! -f $direntry);

    if(($direntry =~ /^[acxbi][0-9][0-9]*\.htm$/) || ($direntry eq 'book1.htm')) {
      move($direntry, $outDir . "/$direntry");
    }
  }
  closedir(DIR);

  logErr("Output file '$outFile' not found") if (! -f $outFile);
  return (-f $outFile) ? 0 : 1;
}



###############################################################################
# sgml2pdf
#
# Generates a PDF file from a SGML file.
#
# OpenJade requires muliple iterations to resolve all referencies. The
# generation terminates in either of the following szenarios:
#    1) The log file does not contain the message  "LaTeX Warning: There were
#       undefined references." and contains the message "Output written on ..."
#       The document could be generated completely.
#    2) The log file contains the message  "LaTeX Warning: There were
#       undefined references." but the referencies cannot be resolved. This is
#       the case when die XXX.aux is identical to the previous
#       XXX.prior.prior.aux file. The document could not be generated properly
#       and an error is returned.
#    3) If more than a configured number of iterations are needed to resolve
#       the referencies, the generation process stops.
#       The document could not be generated properly and an error is returned.
#
# return  (a scalar) 0 on success
###############################################################################
sub sgml2pdf {

  my $outFile    = $outDir . "/" . $inFileName . ".pdf";
  my $outLogTmp  = docgen . ".log.tmp";
  my $passs      = 1;   # current generation pass
  my $searchhits = 0;   #
  my $maxpasses  = 5;   # a limit for the generation passes
  my $command    = "";
  my $success    = 0;
  my $error      = 0;
  my $start      = time();



  # cleanup
  unlink $outFile;
  unlink $outLogTmp;

  # PHASE 1: TEX generation
  logHeader($outLog, "SGML to PDF ($inFile) phase 1:  TEX generation");

  $command = "$ENV{'DOCGEN_HOME'}/openjade-1.3.1/bin/openjade " .
             "-t tex -f $outLogTmp -V tex-backend " .
             "-d $ENV{'DOCGEN_HOME'}/docbook-dsssl-1.78/print/docbook.dsl $inFile";
  logMsg("Executing: $command");
  system ($command);

  # append temporary log output to logfile
  appendFile($outLog, $outLogTmp, "");
  unlink $outLogTmp;   # created by jadetex


  # PHASE 2: PDF generation
  do {
    logHeader($outLog, "SGML to PDF ($inFile) phase 2:  PDF generation pass $pass");

    $command = "$ENV{'MIKTEX_HOME'}/miktex/bin/pdfjadetex -max-strings=1000000 -pool-size=1000000 " .
               $inFileName . ".tex";
    logMsg("Executing: $command");
    system ($command);

    # log output to logfile
    appendFile($outLog, $outDir . "/" . $inFileName . ".log", "");

    # Check for undefined referencies warning
    $searchhits = fileGrep($inFileName . ".log",
                           "LaTeX Warning: There were undefined references.");

    unlink($inFileName . ".log");

    if ($searchhits == 0) {
      # Check message "Output written on ..."
      $searchhits = fileGrep($inFileName . ".log", "Output written on $inFileName.pdf");
      if ($searchhits == 0) {
        logMsg("Successful document generation.");
        $success = 1;
      }else{
        logErr("\"Output written on ...\" message not found.");
        $error = 1;
      }
    }else{
      logMsg("Found \"LaTeX Warning: There were undefined references.\" ".
                      "message. Going into next generation pass.");

      if (-f $inFileName . ".prior.prior.aux") {
        logMsg("Checking *.aux files.");

        # Compare the *.aux files. The docgen is finished if they are
        # identical.
        if (compare($inFileName . ".aux", $inFileName . ".prior.prior.aux") == 0) {
          logMsg("*.aux files are identical. Stop.");
          $success = 1;
        }else{
          logMsg("*.aux files are not identical. Stop.");
        }
      }

      if ($success == 0) {
        $pass++;

        # Limit the number of generation passes
        if ($pass > $maxpasses) {
          logErr("Max number of passes reached. Aborting.");
          $error = 1;
        }

        # Create copies of the *.aux files
        (-f $inFileName . ".prior.aux") && copy($inFileName . ".prior.aux", $inFileName . ".prior.prior.aux");
        (-f $inFileName . ".aux") && copy($inFileName . ".aux", $inFileName . ".prior.aux");
      }
    }
  } while (($success == 0) && ($error == 0));

  if ($debug) {
    # Keep the intermediate files in debug mode
    logDbg("Keeping the intermediate files in debug mode");
    move($inFileName . ".tex"            , $outDir);
    move($inFileName . ".aux"            , $outDir);
    move($inFileName . ".prior.aux"      , $outDir);
    move($inFileName . ".prior.prior.aux", $outDir);
    move($inFileName . ".out"            , $outDir);
  }else{
    unlink($inFileName . ".tex");
    unlink($inFileName . ".aux");
    unlink($inFileName . ".prior.aux");
    unlink($inFileName . ".prior.prior.aux");
    unlink($inFileName . ".out");
  }

  if ($success == 1) {
    # Move the pdf file on success
    move($inFileName . ".pdf", $outFile);
    if (-f $outFile) {
      $elapsed = time() - $start;
      logMsg("SGML to PDF ($inFile) elapsed time: $elapsed sec");
      return 0;
    }else{
      logErr("Output file '$outFile' not found");
    }
  }

  # On any error cleanup and return
  unlink($inFileName . ".pdf");
  logErr("SGML to PDF conversion on $inFile failed");

  return 1;
}



###############################################################################
# logHeader
#
# Logs a header message to standard output and a log file
#
# $file  a filename to log to
# $msg   a header message to log
# return -
###############################################################################
sub logHeader($file, $msg) {
  my ($file, $msg) = @_;

  logMsg("****************************************************************************");
  logMsg("* $msg");
  logMsg("****************************************************************************");
}



###############################################################################
# logDbg
#
# If debug is enabled logs a message to standard output and to a log file
#
# $msg   a message to log
# return -
###############################################################################
sub logDbg($msg) {
  my ($msg) = @_;
  my $ts = timestamp();

  if ($debug) {
    print("$prog:DBG:$ts> $msg\n") if (!$quiet);
    appendText2File($outLog, "$prog:DBG:$ts> $msg\n");
  }
}



###############################################################################
# logMsg
#
# Logs a message to standard output and to a log file
#
# $msg   a message to log
# return -
###############################################################################
sub logMsg($msg) {
  my ($msg) = @_;
  my $ts = timestamp();

  print("$prog:MSG:$ts> $msg\n") if (!$quiet);
  appendText2File($outLog, "$prog:MSG:$ts> $msg\n");
}



###############################################################################
# logErr
#
# Logs an error  message to standard output and to a log file
#
# $msg   a message to log
# return -
###############################################################################
sub logErr($msg) {
  my ($msg) = @_;
  my $ts = timestamp();

  print("$prog:ERR:$ts> $msg\n");
  appendText2File($outLog, "$prog:ERR:$ts> $msg\n");
}



###############################################################################
# timestamp
#
# Returns a formatted timestamp in the format "DD.MM.YYYY HH:MM:SS"
#
# return  a formatted timestamp string
###############################################################################
sub timestamp() {
  my $ts = sprintf("%02d.%02d.%04d %02d:%02d:%02d",
                   localtime->mday(),
                   localtime->mon() + 1,
                   localtime->year() + 1900,
                   localtime->hour(),
                   localtime->min(),
                   localtime->sec());

  return $ts;
}



###############################################################################
# appendFile
#
# Appends one file to another
#
# $toFile     a filename to which the other file is appended
# $fromFile   a file to be appended
# $prefix     a prefix for each line
# return      -
###############################################################################
sub appendFile($toFile, $fromFile, $prefix) {
  my ($toFile, $fromFile, $prefix) = @_;

  open(TO,   ">>" . $toFile);
  open(FROM, "<" . $fromFile);
  while(<FROM>) { printf(TO "%s%s",$prefix,$_); }
  close(FROM);
  close(TO);
}



###############################################################################
# appendText2File
#
# Appends text to a file
#
# $toFile     a filename to which the text is appended
# $text       a text to be appended
# return      -
###############################################################################
sub appendText2File($toFile, $text) {
  my ($toFile, $text) = @_;

  open(TO, ">>" . $toFile);
  print TO $text;
  close(TO);
}



###############################################################################
# fileGrep
#
# Returns the number of lines that contain a given text
#
# $file   a file given by a filename to check
# $text   a text to be grep'd
# return  (a scalar) the of matches
###############################################################################
sub fileGrep($file, $text) {
  my ($file, $text) = @_;
  my $hits = 0;

  open(FILE, "<" . $file);
  while(<FILE>) {
    if (/$text/) { $hits++; }
  }
  close(FILE);

  return $hits;
}



###############################################################################
# usage
#
# Prints a usage
#
# return -
###############################################################################
sub usage {
  print "usage: $prog -f format -i infile [-o outdir] [-d] [h] [-q] [-v]\n";
  print "       -f format  the generated output format {html|htmlsingle|pdf|rtf}\n";
  print "                  html:       produces multiple HTML files\n";
  print "                  htmlsingle: produces a single HTML file\n";
  print "                  pdf:        produces a PDF file\n";
  print "                  rtf:        produces a RTF file\n";
  print "       -i infile  a sgml input file\n";
  print "       -o outdir  an existing output directory. Default is the \n";
  print "                  current working directory.\n";
  print "       -d         enable debugging\n";
  print "       -q         quiet mode\n";
  print "       -h         prints this usage\n";
  print "       -v         prints the version number\n";
  print "\n";
  print "   E.g.  $prog -f html -i sample.sgml -o out\n";
  print "\n";
  print "   The script creates a single log file (<infile>.log) in the\n";
  print "   output directory.\n";
  print "\n";
  print "   Prerequisites: \n";
  print "\n";
  print "       - The environment variable 'DOCGEN_HOME' must be set and\n";
  print "         points to the docgen installation directory.\n";
  print "         e.g.: DOCGEN_HOME=C:\pgm\docgen\n";
  print "       - The environment variable 'MIKTEX_HOME' must be set and\n";
  print "         points to the tex installation directory.\n";
  print "         e.g.: MIKTEX_HOME=C:\pgm\tex\texmf\n";
  print "       - The tool 'perl' must be reachable through the PATH\n";
  print "         environment variable.\n";
  print "\n";
  exit 1;
}


