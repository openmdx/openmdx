#!/bin/sh

if [ $1 = "-" ]
then
    sourcePath=$2
    targetPath=$3
    sourceFile=$4
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
    find $sourcePath -name "*.dot" -exec $0 - ${sourcePath} ${targetPath} {} \;
fi
