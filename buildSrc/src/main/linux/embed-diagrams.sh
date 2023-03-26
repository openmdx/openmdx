#!/bin/sh
#
# ====================================================================
# Project:     openMDX/Core, http://www.openmdx.org/
# Description: Embed Diagrams 
# Owner:       the original authors.
# ====================================================================
#
# This software is published under the BSD license as listed below.
# 
# Redistribution and use in source and binary forms, with or
# without modification, are permitted provided that the following
# conditions are met:
# 
# * Redistributions of source code must retain the above copyright
#   notice, this list of conditions and the following disclaimer.
# 
# * Redistributions in binary form must reproduce the above copyright
#   notice, this list of conditions and the following disclaimer in
#   the documentation and/or other materials provided with the
#   distribution.
# 
# * Neither the name of the openMDX team nor the names of its
#   contributors may be used to endorse or promote products derived
#   from this software without specific prior written permission.
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
# This product includes software developed by other organizations as
# listed in the NOTICE file.
#

if [ $1 = "--help" ]
then
	echo "Usage: $0 ‹source-tree› ‹target-tree›"
	echo "       Converts DOT files from the source tree to SVG files using Graphviz and embeds them into"
	echo "       the corresponding HTML files in the target tree by replacing the \${SVG} place holders"
elif [ $1 = "--dot" ]
then
    sourceFile=$2
    sourcePath=$3
    targetPath=$4
    relativeStart=1+${#sourcePath}
    relativePath=${sourceFile:${relativeStart}}
    targetFile=${targetPath}/${relativePath%.*}.html
    tempFile=${targetFile%.*}.tmp
    targetFile=${targetPath}/${relativePath%.*}.html
    imageFile=${targetPath}/${relativePath%.*}.svg
    echo "${sourceFile} -> ${imageFile} -> ${targetFile}"
    dot -Tsvg -o ${imageFile} ${sourceFile} 
    xmllint --xpath "//*[name()='svg']" ${imageFile} | sed '/${SVG}/{
        s/${SVG}//g
        r /dev/stdin
    }' ${targetFile}  | sed '{
        s/height="[^"]*"//
        s/width="[^"]*"//
    }' > ${tempFile}
    mv -f ${tempFile} ${targetFile}
else
    sourcePath=$1
    targetPath=$2
    find $sourcePath -name "*.dot" -exec $0 --dot {} ${sourcePath} ${targetPath} \;
fi
