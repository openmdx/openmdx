'/////////////////////////////////////////////////////////////////////////////
'// Project:     openmdx, http://www.openmdx.org/
'// Name:        $Id: ppt2sgml.vba,v 1.2 2004/02/10 16:03:08 jlang Exp $
'// Description: Powerpoint to SGML exporter
'// Revision:    $Revision: 1.2 $
'// Owner:       OMEX AG, Switzerland, http://www.omex.ch
'// Date:        $Date: 2004/02/10 16:03:08 $
'/////////////////////////////////////////////////////////////////////////////
'//
'// This software is published under the BSD license
'// as listed below.
'// 
'// Copyright (c) 2004, OMEX AG, Switzerland
'// All rights reserved.
'// 
'// Redistribution and use in source and binary forms, with or without 
'// modification, are permitted provided that the following conditions 
'// are met:
'// 
'// Redistribution and use in source and binary forms, with or
'// without modification, are permitted provided that the following
'// conditions are met:
'// 
'// * Redistributions of source code must retain the above copyright
'// notice, this list of conditions and the following disclaimer.
'// 
'// * Redistributions in binary form must reproduce the above copyright
'// notice, this list of conditions and the following disclaimer in
'// the documentation and/or other materials provided with the
'// distribution.
'// 
'// * Neither the name of the openMDX team nor the names of its
'// contributors may be used to endorse or promote products derived
'// from this software without specific prior written permission.
'// 
'// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
'// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
'// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
'// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
'// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
'// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
'// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
'// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
'// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
'// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
'// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
'// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
'// POSSIBILITY OF SUCH DAMAGE.
'// 
'// ------------------
'// 
'// This product includes software developed by the Apache Software
'// Foundation (http://www.apache.org/).
'/////////////////////////////////////////////////////////////////////////////

Const DOC_TYPE = "<!DOCTYPE book PUBLIC ""-//OASIS//DTD DocBook V3.1//EN"" ["
Const DOCUMENT_SOURCE = "<!-- Powerpoint -->"
Const END_OF_ENTITY = "]>"

Dim aLevelName(6) As String
Dim lastSlideTitle As String
Dim lastSectionLevel As Integer
Dim pictureExport As Presentation

' set in ppt2sgml, used in presentation2sgml
Dim exportWindow As DocumentWindow

' variables for log messages:
' logErrText will be written first to the log file,
' followed by logWarn1Text, logWarn2Text, logWarn3Text;
' (this facilitates easy debugging!)
Dim logErrText, logWarn1Text, logWarn2Text As String

' Macros defined on slides with name [MACRO]
Dim macroNames(1000) As String
Dim macroValues(1000) As String
Dim nMacros As Integer


'------------------------------------------------------------------------------
Sub ppt2sgml()
    ' remember current window and slide to reset at to end of export
    Dim currentSlide As Slide
    Dim currentWindow, wndIterator As DocumentWindow
    
    Set currentWindow = Application.ActiveWindow
    ActiveWindow.ViewType = ppViewNormal
    ActiveWindow.Panes.Item(2).Activate
    Set currentSlide = currentWindow.View.Slide
    
    logErrText = ""
    logWarn1Text = ""
    logWarn2Text = ""
        
    sgmlFileName = currentWindow.Presentation.Path & "\..\sgml\" & currentWindow.Presentation.Name
    If Right$(sgmlFileName, 4) = ".ppt" Then
      sgmlFileName = Mid$(sgmlFileName, 1, Len(sgmlFileName) - 4)
    End If
    
    ' macros
    nMacros = 0
    
    aLevelName(0) = "book"
    aLevelName(1) = "chapter"
    aLevelName(2) = "sect1"
    aLevelName(3) = "sect2"
    aLevelName(4) = "sect3"
    aLevelName(5) = "sect4"
    aLevelName(6) = "sect5"

    ' lookup picture-export.ppt. This temporary presentation is
    ' required to export pictures
    Dim oPresentations As Presentations
    Dim oPresentation As Presentation
    Set oPresentations = Application.Presentations
    pictureExportFound = False
    For Each wndIterator In Application.Windows
      If wndIterator.Presentation.Name = "picture-export.ppt" Then
        Set pictureExport = wndIterator.Presentation
        Set exportWindow = wndIterator
        pictureExportFound = True
      End If
    Next
    If Not pictureExportFound Then
      MsgBox "Presentation picture-export.ppt not found"
      Exit Sub
    End If
    currentWindow.Activate
    
    If currentWindow.Presentation.Slides.Count > 0 Then
    
        ' export to sgml.
        ' #1 = document body
        ' #2 = document header
        Close #1
        Close #2
        Open sgmlFileName & ".body.sgm" For Output Shared As #1
        Open sgmlFileName & ".header.sgm" For Output Shared As #2
        Print #2, DOC_TYPE
        Print #2, DOCUMENT_SOURCE
        
        lastSectionLevel = 0
        lastSlideTitle = ""
        
        Call presentation2sgml(currentWindow.Presentation.Slides)
        
        ' end of book
        For iLoop = lastSectionLevel To 1 Step -1
            Print #1, "</" & aLevelName(iLoop) & ">"
        Next
        Print #1, "&PPTnull;</" & aLevelName(0) & ">"
        Print #2, "<!ENTITY PPTnull " & Chr(34) & Chr(34) & ">"
        Print #2, END_OF_ENTITY
        Call WriteBookInfo
                
        ' Append #1 to #2
        Close #1
        Open sgmlFileName & ".body.sgm" For Input Shared As #1
        Do While Not EOF(1)
            Line Input #1, sLine
            Print #2, sLine
        Loop
        Close #1
        Close #2
        
        ' Completed book and delete temporary files
        FileCopy sgmlFileName & ".header.sgm", sgmlFileName & ".sgm"
        Kill sgmlFileName & ".header.sgm"
        Kill sgmlFileName & ".body.sgm"
        
        ' write logText
        Close #9
        Open sgmlFileName & ".export.log" For Output Shared As #9
        If logErrTxt <> "" Then Print #9, logErrText & vbCrLf & vbCrLf
        If logWarn1Text <> "" Then Print #9, logWarn1Text & vbCrLf & vbCrLf
        If logWarn2Text <> "" Then Print #9, logWarn2Text & vbCrLf & vbCrLf
        Close #9
    End If
    currentWindow.Activate
    currentSlide.Select
    
End Sub

'------------------------------------------------------------------------------
'
' Exports the slides as SGML
' Uses the vars aLevelName, lastSlideTitle, lastSectionLevel, pictureExport
'
Private Sub presentation2sgml(oSlides As Slides)

    Dim oSlide As Slide
    Dim oShape As Shape
    Dim oWindow As DocumentWindow
    Dim slideTitle As String
    Dim sectionLevel As Integer
    
    ' character position of processing is inside
    ' program listing.
    Dim isPreFormatted As Boolean
    
    ' true, if there is a picture or table on the slide;
    ' used to check missing object insertions.
    Dim insertSlideObject As Boolean
    
    Set oWindow = Application.ActiveWindow
    For Each oSlide In oSlides
        isPreFormatted = False
        ' print sgml comment for easier debugging
        Print #1, vbCrLf & "<!-- " & oWindow.Presentation.Name & " : slide " & Trim(Str(oSlide.SlideNumber)) & "-->"
        insertSlideObject = False
        
        oSlide.Select
        
        If Not oSlide.Shapes.HasTitle Then
            Call LogMsg("E", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: slide does not have a title")
            GoTo exitfor
        End If
                    
        ' write end tags; write start of new section;
        ' get title and title tag
        slideTitle = Trim(oSlide.Shapes.title.TextFrame.TextRange.text)
        
        ' [NOTE]
        If Mid$(slideTitle, 1, 6) = "[NOTE]" Then
          ' skip slide without any action
          
        ' [INCLUDE]
        ElseIf Mid$(slideTitle, 1, 9) = "[INCLUDE]" Then
            Dim nestedPresentation As Presentation
            Set nestedPresentation = Application.Presentations.Open(oWindow.Presentation.Path & "/" & Mid$(slideTitle, 11))
            ActiveWindow.ViewType = ppViewNormal
            ActiveWindow.Panes.Item(2).Activate
            Call presentation2sgml(nestedPresentation.Slides)
            nestedPresentation.Close
            oWindow.Activate
        
        ' [INCLUDE-FILE;x]:  close previous levels down to level x
        ' [INCLUDE-FILE]:    if no x is specified, default is 2 for downward compatibility
        '                    reasons (to include SGML files produced by the ModelExternalizer)
        ElseIf Mid$(slideTitle, 1, 13) = "[INCLUDE-FILE" Then
            Dim closeLevelPos As Long
            Dim closeLevel As Integer
            closeLevelPos = InStr(14, slideTitle, "]")
            If closeLevelPos > 14 Then
                closeLevel = Val(Trim(Mid(slideTitle, 15, closeLevelPos - 15)))
            Else
                Call LogMsg("E", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: Title tag must be of the form: [S<level>;tagId] or [A;tagId] or [INCLUDE] or [INCLUDE-FILE] or [MACROS]")
                GoTo exitfor
            End If
            For iLoop = lastSectionLevel To closeLevel Step -1
                 Print #1, "</" & aLevelName(iLoop) & ">"
            Next
            lastSectionLevel = closeLevel - 1
            sectionLevel = closeLevel
            
            Dim fileName As String
            fileName = oWindow.Presentation.Path & "\" & oSlide.Shapes.title.AlternativeText
            Open fileName For Input Shared As #3
            Do While Not EOF(3)
                Line Input #3, sLine
                Print #1, sLine
            Loop
            Close #3
        
        ' [MACROS]
        ElseIf slideTitle = "[MACROS]" Then
            Print #2, "<!-- MACROS -->"
            For Each oShape In oSlide.NotesPage.Shapes
                If oShape.HasTextFrame Then
                    
                    Print #2, oShape.TextFrame.TextRange.text
                    
                    ' Collect Macros. The definitions are of the form
                    ' {<macro name>   <macro definition>}
                    i = 0
                    While i < oShape.TextFrame.TextRange.Characters.Length()
                      i = i + 1
                      c = oShape.TextFrame.TextRange.Characters(i, 1)
                      ' skip blanks
                      While (c <= " ") And (i < oShape.TextFrame.TextRange.Characters.Length())
                        i = i + 1
                        c = oShape.TextFrame.TextRange.Characters(i, 1)
                      Wend
                      ' macro name
                      macroName = ""
                      While (c > " ") And (i < oShape.TextFrame.TextRange.Characters.Length())
                        macroName = macroName + c
                        i = i + 1
                        c = oShape.TextFrame.TextRange.Characters(i, 1)
                      Wend
                      ' skip blanks
                      While (c <= " ") And (i < oShape.TextFrame.TextRange.Characters.Length())
                        i = i + 1
                        c = oShape.TextFrame.TextRange.Characters(i, 1)
                      Wend
                      ' macro value
                      macroValue = ""
                      While (c <> vbCrLf) And (i < oShape.TextFrame.TextRange.Characters.Length())
                        macroValue = macroValue + c
                        i = i + 1
                        c = oShape.TextFrame.TextRange.Characters(i, 1)
                      Wend
                      If c > " " Then
                        macroValue = macroValue + c
                      End If
                      macroNames(nMacros) = macroName
                      macroValues(nMacros) = macroValue
                      nMacros = nMacros + 1
                    Wend
                End If
            Next
            Print #2, "<!-- MACROS -->"
        
        ' [S...] | [A...]
        Else
            lBracketPos = InStr(slideTitle, "[")
            rBracketPos = InStr(slideTitle, "]")
            titleTagPos = InStr(slideTitle, ";")
            If (lBracketPos <> 1) Or (rBracketPos = 0) Or (titleTagPos = 0) Then
                Call LogMsg("E", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: Title tag must be of the form: [S<level>;tagId] or [A;tagId] or [INCLUDE] or [INCLUDE-FILE] or [MACROS]")
                GoTo exitfor
            End If
        
            ' set aLevelName(1). Is either chapter or appendix
            sectionType = Mid(slideTitle, 2, 1)
            If (sectionType <> "S") And (sectionType <> "A") Then
                Call LogMsg("E", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: Title type must be either S (= section) or A (= appendix)")
                GoTo exitfor
            End If
            
            ' titleTag (replace " " and "_" with "-"
            Dim titleTag As String
            titleTag = Trim(Mid(slideTitle, titleTagPos + 1, rBracketPos - 1 - titleTagPos))
            For i = 1 To Len(titleTag)
              If (Mid$(titleTag, i, 1) = " ") Or (Mid$(titleTag, i, 1) = "_") Then
                Mid$(titleTag, i, 1) = "-"
                Call LogMsg("W2", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: Title tag contains " _
                & Chr(34) & Mid$(titleTag, i, 1) & Chr(34) & " character at position " & Trim(Str(i)) & _
                "; automatic translation to hyphens could lead to duplicate title tags!")
              End If
            Next i
            
            ' level
            sectionLevel = Mid(slideTitle, 3, 1)
            
            ' strippedTitle
            strippedTitle = Trim(Right(slideTitle, Len(slideTitle) - rBracketPos))
            slideTitle = strippedTitle
            If Left(slideTitle, 1) = Chr(11) Then
                strippedTitle = Trim(Right(slideTitle, Len(slideTitle) - 1))
            End If
            strippedTitle = Replace(strippedTitle, "&", "&amp;")
            strippedTitle = Replace(strippedTitle, "<", "&lt;")
            strippedTitle = Replace(strippedTitle, ">", "&gt;")
            strippedTitle = Replace(strippedTitle, "^«", "&laquo;")
            
            ' new section?
            If (lastSlideTitle = slideTitle) And (lastSectionLevel <> sectionLevel) Then
                Call LogMsg("E", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: Title level mismatch. slideTitle=" & slideTitle & "; lastSectionLevel=" & lastSectionLevel & "; sectionLevel=" & sectionLevel)
                GoTo exitfor
            End If
            
            ' new section
            If lastSlideTitle <> slideTitle Then
            
                ' close current levels
                For iLoop = lastSectionLevel To sectionLevel Step -1
                    Print #1, "</" & aLevelName(iLoop) & ">"
                Next
                
                ' switch name of top-level chapter|appendix
                If sectionLevel = 1 Then
                  If sectionType = "S" Then
                    aLevelName(1) = "chapter"
                  ElseIf sectionType = "A" Then
                    aLevelName(1) = "appendix"
                  End If
                End If
        
                ' open new level
                If (sectionLevel > lastSectionLevel) And (sectionLevel <> lastSectionLevel + 1) Then
                    Call LogMsg("E", "[" & curPres & ", slide " & oSlide.SlideNumber & "]: Incorrect level change from " & lastSectionLevel & " to " & sectionLevel)
                    GoTo exitfor
                End If
            
                ' print start tag with id attribute and title child element
                If titleTag = "" Then
                    Print #1, "<" & aLevelName(sectionLevel) & ">"
                Else
                    Print #1, "<" & aLevelName(sectionLevel) & " id = " & Chr(34) & "s-" & titleTag & Chr(34) & " xreflabel = " & Chr(34) & Replace(strippedTitle, "/", "-") & Chr(34) & ">"
                End If
                
                ' print title child element
                If sectionLevel <> 0 Then
                    Print #1, "<title>" & strippedTitle & "</title>"
                End If
                
            End If
            
            If titleTag <> "" Then
                Print #2, "<!-- slidetag=" & titleTag & " -->"
            End If
            
            picRef = "../images/" & titleTag & ".png"
            picRefEnlarged = "../images/" & titleTag & "_enlarged.png"
            
            lastSectionLevel = sectionLevel
            lastSlideTitle = slideTitle
                    
            ' select all shapes of slide (except title)
            currentTitleId = oSlide.Shapes.title.Id
            entityId = "e" & Round((1000000# * Rnd()), 0)

            Dim alternativeTitle As String
            alternativeTitle = strippedTitle
            Dim nShapes As Integer
            Dim scaleFactorX, scaleFactorY, pictureWidth, pictureHeight As Double
            Dim tableOnSlide As Boolean
            
            tableOnSlide = False
            nShapes = 0
            ActiveWindow.Selection.Unselect
            For Each oShape In oSlide.Shapes
            
                ' special treatment of slide title
                If currentTitleId = oShape.Id Then
                    ' if .AlternativeText is explicitly set
                    If (Left(oShape.AlternativeText, 10) <> "Text Box: ") Then
                        alternativeTitle = oShape.AlternativeText
                        alternativeTitle = Replace(alternativeTitle, "&", "&amp;")
                        alternativeTitle = Replace(alternativeTitle, "<", "&lt;")
                        alternativeTitle = Replace(alternativeTitle, ">", "&gt;")
                        alternativeTitle = Replace(alternativeTitle, "^«", "&laquo;")
                    End If
                    
                ' slide contains table
                ElseIf oShape.HasTable Then
                    ' do not include tables in graphic selection
                    insertSlideObject = True
                    tableOnSlide = True
                    
                ' If any shape is tagged as NOTE do not export a picture for this slide
                ElseIf Left(oShape.AlternativeText, 4) = "NOTE" Then
                  nShapes = 0
                  
                ' enlarge picture?
                Else
                    oShape.Select (nShapes = 0)
                    scaleFactorX = 1#
                    If Left(oShape.AlternativeText, 8) = "ENLARGE=" Then
                      scaleFactorX = Val(Mid(oShape.AlternativeText, 9))
                    End If
                    scaleFactorY = scaleFactorX
                    pictureWidth = oShape.Width
                    pictureHeight = oShape.Height
                    nShapes = nShapes + 1
                End If
            Next
            If tableOnSlide Then
                nShapes = 0
            ElseIf nShapes > 0 Then
                insertSlideObject = True
            End If
            
            ' Text (from notes page)
            bNotePageWritten = False
            If oSlide.NotesPage.Count > 0 Then
                For Each oShape In oSlide.NotesPage.Shapes
                    If oShape.HasTextFrame Then
                        Dim text, outText As String
                        Dim isBold, isItalic As Boolean
                        isBold = False
                        isItalic = False
                        outText = "<para>"
                        i = 1
                        While i <= oShape.TextFrame.TextRange.Characters.Length
                            ' use "^" as escape character for "«"
                            If oShape.TextFrame.TextRange.Characters(i, 1) = "^" And _
                               oShape.TextFrame.TextRange.Characters(i + 1, 1) = "«" Then
                               ' use mnemonic for "angle quotation mark, left"
                               outText = outText & "&laquo;"
                               i = i + 2
                            End If
                            
                            ' insert figure
                            If "«figure»" = oShape.TextFrame.TextRange.Characters(i, 8) Then
                                insertSlideObject = False
                                outText = outText & "<figure id = " & Chr(34) & "f-" & titleTag & Chr(34) & ">"
                                outText = outText & "<title>" & alternativeTitle & "</title>"
                                outText = outText & "<mediaobject>"
                                outText = outText & "<imageobject><imagedata entityref = " & Chr(34) & entityId & Chr(34) & "></imageobject>"
                                If (scaleFactorX <> 1#) Or (scaleFactorY <> 1#) Then
                                    outText = outText & vbCrLf & "<caption><para><ulink url=""" & picRefEnlarged & ".htm""><citetitle> enlarge</citetitle></ulink></para></caption>" & vbCRFL
                                End If
                                outText = outText & "</mediaobject></figure>"
                                i = i + 8
                            
                            ' insert table
                            ElseIf "«table»" = oShape.TextFrame.TextRange.Characters(i, 7) Then
                                Dim tableShape As Shape
                                insertSlideObject = False
                                For Each tableShape In oSlide.Shapes
                                    If tableShape.HasTable Then
                                        outText = outText & "</para>"
                                        Call WriteTable(tableShape.table, titleTag, alternativeTitle, outText)
                                        outText = outText & "<para>"
                                    End If
                                Next
                                i = i + 7
                            
                            ' <pre>  preserves white space and line breaks
                            ElseIf "«pre" = oShape.TextFrame.TextRange.Characters(i, 4) Then
                                i = i + 4
                                  exampleTitle = ""
                                  ' skip separator
                                  i = i + 1
                                  While "»" <> oShape.TextFrame.TextRange.Characters(i, 1)
                                      exampleTitle = exampleTitle & oShape.TextFrame.TextRange.Characters(i, 1)
                                      i = i + 1
                                  Wend
                                  outText = outText & "<example><title>" & Trim(exampleTitle) & "</title><programlisting>"
                                  i = i + 1
                                  isPreFormatted = True
                            ElseIf "«/pre»" = oShape.TextFrame.TextRange.Characters(i, 6) Then
                                i = i + 6
                                outText = outText & "</programlisting></example>"
                                isPreFormatted = False
                              
                            ' <listing> behaves like <pre> but the listing
                            ' is not included in the table of examples
                            ' during the document generation process.
                            ElseIf "«listing»" = oShape.TextFrame.TextRange.Characters(i, 9) Then
                                outText = outText & "<screen>"
                                i = i + 9
                                isPreFormatted = True
                            ElseIf "«/listing»" = oShape.TextFrame.TextRange.Characters(i, 10) Then
                                outText = outText & "</screen>"
                                i = i + 10
                                isPreFormatted = False
                              
                            ' <ol>
                            ElseIf "«ol»" = oShape.TextFrame.TextRange.Characters(i, 4) Then
                              outText = outText & "<orderedlist>"
                              i = i + 4
                            ElseIf "«/ol»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                              outText = outText & "</orderedlist>"
                              i = i + 5
                            
                            ' <ul>
                            ElseIf "«ul»" = oShape.TextFrame.TextRange.Characters(i, 4) Then
                              outText = outText & "<itemizedlist>"
                              i = i + 4
                            ElseIf "«/ul»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                              outText = outText & "</itemizedlist>"
                              i = i + 5
                            
                            ' <subscript>
                            ElseIf "«sub»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                              outText = outText & "<subscript>"
                              i = i + 5
                            ElseIf "«/sub»" = oShape.TextFrame.TextRange.Characters(i, 6) Then
                              outText = outText & "</subscript>"
                              i = i + 6
                            
                            ' <superscript>
                            ElseIf "«sup»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                              outText = outText & "<superscript>"
                              i = i + 5
                            ElseIf "«/sup»" = oShape.TextFrame.TextRange.Characters(i, 6) Then
                              outText = outText & "</superscript>"
                              i = i + 6
                            
                            ' <li>
                            ElseIf "«li»" = oShape.TextFrame.TextRange.Characters(i, 4) Then
                              outText = outText & "<listitem><para>"
                              i = i + 4
                            ElseIf "«/li»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                          ' within a list item, emphasised formating must be turned off
                          ' BEFORE the paragraph's end tag (i.e. before EOL processing)
                                If isBold Or isItalic Then
                                    outText = outText & "</emphasis>"
                                    isBold = False
                                    isItalic = False
                                End If
                              outText = outText & "</para></listitem>"
                              i = i + 5
                                                        
                            ' <code>
                            ElseIf "«code»" = oShape.TextFrame.TextRange.Characters(i, 6) Then
                              outText = outText & "<literal>"
                              i = i + 6
                            ElseIf "«/code»" = oShape.TextFrame.TextRange.Characters(i, 7) Then
                              outText = outText & "</literal>"
                              i = i + 7
                            
                            ' <var>
                            ElseIf "«var»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                              outText = outText & "<literal>"
                              i = i + 5
                            ElseIf "«/var»" = oShape.TextFrame.TextRange.Characters(i, 6) Then
                              outText = outText & "</literal>"
                              i = i + 6
                            
                            ' <warning>
                            ElseIf "«warning»" = oShape.TextFrame.TextRange.Characters(i, 9) Then
                                outText = outText & "<blockquote><inlinemediaobject><imageobject><imagedata fileref=""../images/warning.png"" format=""png"" valign=""bottom""></imageobject></inlinemediaobject>&#32;&#32;"
                                i = i + 9
                            ElseIf "«/warning»" = oShape.TextFrame.TextRange.Characters(i, 10) Then
                                outText = outText & "</blockquote>"
                                i = i + 10
                            
                            ' <caution>
                            ElseIf "«caution»" = oShape.TextFrame.TextRange.Characters(i, 9) Then
                                outText = outText & "<blockquote><inlinemediaobject><imageobject><imagedata fileref=""../images/caution.png"" format=""png"" valign=""bottom""></imageobject></inlinemediaobject>&#32;&#32;"
                                i = i + 9
                            ElseIf "«/caution»" = oShape.TextFrame.TextRange.Characters(i, 10) Then
                                outText = outText & "</blockquote>"
                                i = i + 10
                            
                            ' <tip>
                            ElseIf "«tip»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                                outText = outText & "<blockquote><inlinemediaobject><imageobject><imagedata fileref=""../images/tip.png"" format=""png"" valign=""bottom""></imageobject></inlinemediaobject>&#32;&#32;"
                                i = i + 5
                            ElseIf "«/tip»" = oShape.TextFrame.TextRange.Characters(i, 6) Then
                                outText = outText & "</blockquote>"
                                i = i + 6
                            
                            ' <important>
                            ElseIf "«important»" = oShape.TextFrame.TextRange.Characters(i, 11) Then
                                outText = outText & "<blockquote><inlinemediaobject><imageobject><imagedata fileref=""../images/important.png"" format=""png"" valign=""bottom""></imageobject></inlinemediaobject>&#32;&#32;"
                                i = i + 11
                            ElseIf "«/important»" = oShape.TextFrame.TextRange.Characters(i, 12) Then
                                outText = outText & "</blockquote>"
                                i = i + 12
                            
                            ' [«label»] is automatically translated to <xref linkend="label">
                            ElseIf "[«" = oShape.TextFrame.TextRange.Characters(i, 2) Then
                              i = i + 2
                              outText = outText & "<emphasis>[<xref linkend="""
                              While oShape.TextFrame.TextRange.Characters(i, 1) <> "»"
                                outText = outText & oShape.TextFrame.TextRange.Characters(i, 1)
                                i = i + 1
                              Wend
                              outText = outText & """>]</emphasis>"
                              i = i + 2
                            
                            ' <h6> (intercept and restart para)
                            ElseIf "«h6»" = oShape.TextFrame.TextRange.Characters(i, 4) Then
                              outText = outText & "</para><bridgehead renderas=""sect4"">"
                              i = i + 4
                            ElseIf "«/h6»" = oShape.TextFrame.TextRange.Characters(i, 5) Then
                          ' within a list item, emphasised formating must be turned off
                          ' BEFORE the paragraph's end tag (i.e. before EOL processing)
                          If isBold Or isItalic Then
                              outText = outText & "</emphasis>"
                              isBold = False
                              isItalic = False
                          End If
                              outText = outText & "</bridgehead><para>"
                              i = i + 5
                                                        
                            ' Macro
                            ElseIf "«" = oShape.TextFrame.TextRange.Characters(i, 1) Then
                                
                                ' set emphasis if content of macro is in bold|italic
                                If oShape.TextFrame.TextRange.Characters(i + 1, 1).Font.Italic Then
                                    If Not (isItalic Or isBold) Then
                                        outText = outText & "<emphasis>"
                                        isItalic = True
                                        isBold = False
                                    Else
                                        If Not isItalic Then
                                            outText = outText & "</emphasis><emphasis>"
                                            isBold = False
                                            isItalic = True
                                        Else
                                            isBold = False
                                        End If
                                    End If
                                ElseIf oShape.TextFrame.TextRange.Characters(i + 1, 1).Font.Bold Then
                                    If Not (isItalic Or isBold) Then
                                        outText = outText & "<emphasis role=""strong"">"
                                        isItalic = False
                                        isBold = True
                                    Else
                                        If Not isBold Then
                                            outText = outText & "</emphasis><emphasis role=""strong"">"
                                            isBold = True
                                            isItalic = False
                                        Else
                                            isItalic = False
                                        End If
                                    End If
                                End If
                                
                                ' macro content
                                macroName = ""
                                i = i + 1
                                While "»" <> oShape.TextFrame.TextRange.Characters(i, 1)
                                    If oShape.TextFrame.TextRange.Characters(i, 1) = vbCrLf Then
                                        ' if EOL before »
                                        Call LogMsg("E", "[" & curPres & ", slide " & oSlide.SlideNumber & "]:  non-terminated macro (end is not on same line)")
                                        GoTo exitfor
                                    End If
                                    If ("_" = outText & oShape.TextFrame.TextRange.Characters(i, 1)) Or (" " = outText & oShape.TextFrame.TextRange.Characters(i, 1)) Then
                                      macroName = macroName & "-"
                                    Else
                                      macroName = macroName & oShape.TextFrame.TextRange.Characters(i, 1)
                                    End If
                                    i = i + 1
                                Wend
                                i = i + 1
                                
                                ' lookup macro and replace macro name with macro value
                                ' If it is a figure, table or section reference do not replace
                                If (Left$(macroName, 2) <> "f-") And (Left$(macroName, 2) <> "t-") And (Left$(macroName, 2) <> "s-") Then
                                    j = 0
                                    found = False
                                    While (j < nMacros) And (Not found)
                                      found = (macroNames(j) = macroName)
                                      If Not found Then
                                        j = j + 1
                                      End If
                                    Wend
                                    If found Then
                                      outText = outText & macroValues(j)
                                    Else
                                      Call LogMsg("E", "[" & curPres & ", slide " & oSlide.SlideNumber & "]:  undefined macro " + macroName)
                                      GoTo exitfor
                                    End If
                                Else
                                  outText = outText & "<xref linkend=""" & macroName & """>"
                                End If
                                                               
                            ' end-of-line
                            ElseIf oShape.TextFrame.TextRange.Characters(i, 1) = vbCrLf Then
                                ' terminate emphasis at end-of-line
                                If isPreFormatted = False Then
                                    If isItalic Or isBold Then
                                      outText = outText & "</emphasis>"
                                      isBold = False
                                      isItalic = False
                                    End If
                                End If
                                outText = outText & oShape.TextFrame.TextRange.Characters(i, 1)
                                i = i + 1
                                
                                ' interprete two consecutive line breaks as new paragraph
                                If isPreFormatted = False Then
                                    While (" " = oShape.TextFrame.TextRange.Characters(i, 1)) _
                                      Or (oShape.TextFrame.TextRange.Characters(i, 1) = vbCrLf)
                                        i = i + 1
                                    Wend
                                    If "«" <> oShape.TextFrame.TextRange.Characters(i, 1) Then
                                        outText = outText & "</para><para>"
                                    End If
                                End If
             
                            ' standard character
                            Else
                              
                                ' <emphasis>
                                If oShape.TextFrame.TextRange.Characters(i, 1).Font.Italic Then
                                    If Not isItalic Then
                                        outText = outText & "<emphasis>"
                                    End If
                                    isItalic = True
                                
                                ' </emphasis> if italic terminates or at end of line
                                ElseIf isItalic Then
                                    outText = outText & "</emphasis>"
                                    isItalic = False
                                
                                ' <emphasis role="strong">
                                Else
                                    If oShape.TextFrame.TextRange.Characters(i, 1).Font.Bold Then
                                        If Not isBold Then
                                            outText = outText & "<emphasis role=""strong"">"
                                        End If
                                        Let isBold = True
                                    ElseIf isBold Then
                                        outText = outText & "</emphasis>"
                                        isBold = False
                                    End If
                                End If
                                
                                ' convert special characters
                                    Select Case oShape.TextFrame.TextRange.Characters(i, 1)
                                    Case "&"
                                        outText = outText & "&amp;"
                                    Case "<"
                                        outText = outText & "&lt;"
                                    Case ">"
                                        outText = outText & "&gt;"
                                    Case ChrW(Val("&H2190")) ' leftward arrow
                                        outText = outText & "&larr;"
                                    Case ChrW(Val("&H2192")) ' rightward arrow
                                        outText = outText & "&rarr;"
                                    Case Else
                                        outText = outText & oShape.TextFrame.TextRange.Characters(i, 1)
                                    End Select
                                
                                i = i + 1
                                
                            End If
                            
                        Wend
                        outText = outText & "</para>"
                        Print #1, outText
                        bNotePageWritten = True
                    End If
                Next
                If insertSlideObject Then
                    Call LogMsg("W1", "[" & oWindow.Presentation.Name & ", slide " & oSlide.SlideNumber & "]: «figure» or «table» is missing in Notes Page pane!")
                End If
                If Not bNotePageWritten Then
                    Print #1, "<para></para>"
                End If
            End If
                        
            ' Graphic (from slide)
            If nShapes > 0 Then
                                   
                ' The export the selected shapes as png is a three step process
                ' 1a) group shapes, if there is more then 1 in the selection
                ' 1b) copy (grouped) shape to clipboard
                ' 2)  from the clipboard paste the picture to the first slide of the presentation to pictureExport
                ' 3a) save the presentation in png format. A directory is created with a file for each slide
                ' 3b) if picture must be scaled, replace the picture in pictureExport
                '     by its exported png (allows scaling of the size of integrated text),
                '     scaled and exported again.
                ' 4) copy first slide (= slide1.png) oWindow.Presentation folder
                '
                ' adjusted pasted picture to top-left:
                ' group shapes first, if there is more then one
                If nShapes > 1 Then
                    ActiveWindow.Selection.ShapeRange.Group
                    ActiveWindow.Selection.SlideRange.Shapes.SelectAll
                End If
                ActiveWindow.Selection.Copy
                If pictureExport.Slides.Count > 0 Then
                    pictureExport.Slides.Item(1).Delete
                End If
                pictureExport.Slides.Add Index:=1, Layout:=ppLayoutBlank
                With pictureExport.PageSetup
                    .SlideWidth = pictureWidth + 2
                    .SlideHeight = pictureHeight + 2
                End With
                Set newShapeRange = pictureExport.Slides.Item(1).Shapes.Paste
                With newShapeRange
                    .Left = 1#
                    .Top = 1#
                End With
                
                ' export whole presentation and get png of first slide
                pictureExport.SaveAs fileName:="c:/temp/picture-export", FileFormat:=ppSaveAsPNG, EmbedTrueTypeFonts:=msoFalse
                FileCopy "c:/temp/picture-export/slide1.png", oWindow.Presentation.Path & "/" & picRef
                
                ' ENLARGE
                If (scaleFactorX <> 1#) Or (scaleFactorY <> 1#) Then
                    With pictureExport.PageSetup
                        .SlideWidth = scaleFactorX * .SlideWidth
                        .SlideHeight = scaleFactorY * .SlideHeight
                    End With
                    Call embedPictureInPage(oWindow.Presentation.Path, picRefEnlarged)
                    pictureExport.SaveAs fileName:="c:/temp/picture-export", FileFormat:=ppSaveAsPNG, EmbedTrueTypeFonts:=msoFalse
                    FileCopy "c:/temp/picture-export/slide1.png", oWindow.Presentation.Path & "/" & picRefEnlarged
                End If
                
                ' mediaobject references to exported picture
                Print #2, "<!ENTITY " & entityId & "  SYSTEM " & Chr(34) & picRef & Chr(34) & " NDATA png>"
            End If
        End If
exitfor:
    Next

End Sub

'------------------------------------------------------------------------------
Private Sub WriteTable(oTable As table, titleTag As String, title As String, ByRef outText As String)

    Dim oCol As Column
    Dim myTable As table
    
    outText = outText & "<table id = " & Chr(34) & "f-" & titleTag & Chr(34) & " frame = " & Chr(34) & "all" & Chr(34) & " colsep = " & Chr(34) & "1" & Chr(34) & " rowsep = " & Chr(34) & "1" & Chr(34) & ">"
    outText = outText & "<title>" & title & "</title>"
    outText = outText & "<tgroup cols = " & Chr(34) & oTable.Columns.Count & Chr(34) & ">"
    
    ' column specifiers. Row(0) contains width specifiers
    iColCount = 1
    For Each oCol In oTable.Columns
        outText = outText & "<colspec colname=""" & Trim(Str(iColCount)) & """ colwidth=""" & oTable.Rows(1).Cells(iColCount).Shape.TextFrame.TextRange.text & """ >"
        iColCount = iColCount + 1
    Next
    
    ' table body
    outText = outText & "<tbody>"
    ' spanning cells in row have same id!
    firstRow = True
    For Each oRow In oTable.Rows
        If Not firstRow Then
            outText = outText & "<row>"
            lShapeId = 0
            iSpanCount = 1
            iColNo = 0
            For Each oCell In oRow.Cells
                iColNo = iColNo + 1
                If lShapeId = oCell.Shape.Id Then
                    iSpanCount = iSpanCount + 1
                    oOldCell = oCell
                ElseIf spanCount > 0 Then
                    outText = outText & "<entry namest = " & Chr(34) & "1" & Chr(34) & " nameend = " & Chr(34) & Trim(Str(iSpanCount)) & Chr(34) & ">"
                    If oOldCell.Shape.HasTextFrame Then
                        outText = outText & "<para>" & oOldCell.Shape.TextFrame.TextRangeText & "</para>"
                    Else
                        Error ("Shape in cell is no TextFrame!")
                    End If
                    outText = outText & "</entry>"
                    lShapeId = 0
                    iSpanCount = 1
                Else
                    outText = outText & "<entry colname = " & Chr(34) & Trim(Str(iColNo)) & Chr(34) & ">"
                    If oCell.Shape.HasTextFrame Then
                    cellText = oCell.Shape.TextFrame.TextRange.text
                    cellText = Replace(cellText, "^«", "&laquo;")
                    outText = outText & "<para>" & cellText & "</para>"
                    Else
                        Error ("Shape in cell is no TextFrame!")
                    End If
                    outText = outText & "</entry>"
                End If
            Next
            outText = outText & "</row>"
        End If
        firstRow = False
    Next
    outText = outText & "</tbody></tgroup>"
    outText = outText & "</table><para></para>"
End Sub


'------------------------------------------------------------------------------
Private Sub WriteBookInfo()

    If activePresentation.BuiltInDocumentProperties("Title").Value = "" Then
       Rem Print "ERROR:  Undefined document property 'Title' !"
       Exit Sub
    Else
        sDPtitle = activePresentation.BuiltInDocumentProperties("Title").Value
        sDocProp = Trim(activePresentation.BuiltInDocumentProperties("Company").Value)
        sDocProp1 = Trim(activePresentation.BuiltInDocumentProperties("Author").Value)
        Print #2, "<book><title>" & sDPtitle & "</title>"
        Print #2, "<bookinfo><title>" & sDPtitle & "</title>"
        Print #2, "<subtitle>" & activePresentation.BuiltInDocumentProperties("Category").Value & "</subtitle>"
        If sDocProp <> "" Then
            Print #2, "<corpauthor>" & sDocProp & "</corpauthor>"
        ElseIf sDocProp1 <> "" Then
            Print #2, "<author><surname>" & sDocProp1 & "</surname>"
        End If
        
        Print #2, "<legalnotice>"
        Print #2, "<para>This software is published under the BSD license"
        Print #2, "as listed below.</para>"
        Print #2, "<para>Copyright (c) 2004, OMEX AG, Switzerland</para>"
        Print #2, "<para>All rights reserved.</para>"
        Print #2, "<para>Redistribution and use in source and binary forms, with or"
        Print #2, "without modification, are permitted provided that the following"
        Print #2, "conditions are met:</para>"
        Print #2, "<para>* Redistributions of source code must retain the above copyright"
        Print #2, "notice, this list of conditions and the following disclaimer.</para>"
        Print #2, "<para>* Redistributions in binary form must reproduce the above copyright"
        Print #2, "notice, this list of conditions and the following disclaimer in"
        Print #2, "the documentation and/or other materials provided with the"
        Print #2, "distribution.</para>"
        Print #2, "<para>* Neither the name of OMEX AG nor the names of the contributors"
        Print #2, "to openMDX may be used to endorse or promote products derived"
        Print #2, "from this software without specific prior written permission.</para>"
        Print #2, "<para>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND"
        Print #2, "CONTRIBUTORS ""AS IS"" AND ANY EXPRESS OR IMPLIED WARRANTIES,"
        Print #2, "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF"
        Print #2, "MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE"
        Print #2, "DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS"
        Print #2, "BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,"
        Print #2, "EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED"
        Print #2, "TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,"
        Print #2, "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON"
        Print #2, "ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,"
        Print #2, "OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY"
        Print #2, "OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE"
        Print #2, "POSSIBILITY OF SUCH DAMAGE.</para>"
        Print #2, "</legalnotice>"
        
        Print #2, "</bookinfo>"
    End If
End Sub

'------------------------------------------------------------------------------
Private Sub embedPictureInPage(baseDir As String, ByVal imagePath As String)
    Close #8
    Open baseDir & "/" & imagePath & ".htm" For Output Shared As #8
        
    Print #8, "<HTML><HEAD><TITLE>Picture in original size</TITLE>"
    Print #8, "<META NAME=""GENERATOR"" CONTENT=""Powerpoin-to-SGML Export"">"
    Print #8, "<BODY CLASS=""SECT1"" BGCOLOR=""#FFFFFF"" TEXT=""#000000"" LINK=""#0000FF"" VLINK=""#840084"" ALINK=""#0000FF"">"
    Print #8, "<DIV CLASS="; SECT1; "><P>"
    Print #8, "<IMG SRC=" & Chr(34) & imagePath & Chr(34) & ">"
    Print #8, "</P></DIV>"
    Print #8, "</BODY>"
    Print #8, "</HTML>"
        
    Close #8
End Sub

'------------------------------------------------------------------------------
Private Sub LogMsg(msgCategory As String, msg As String)
    Select Case msgCategory
    Case "E"
        logErrText = logErrText & "ERROR" & msg & vbCrLf
    Case "W1"
        logWarn1Text = logWarn1Text & "WARNING" & msg & vbCrLf
    Case "W2"
        logWarn2Text = logWarn2Text & "WARNING" & msg & vbCrLf
    Case Else
    End Select
End Sub

'--- End of File -----------------------------------------------------------------
