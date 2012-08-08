'/////////////////////////////////////////////////////////////////////////////
'// Project:     openmdx, http://www.openmdx.org/
'// Name:        $Id: ChangeToHyperlink.vba,v 1.2 2004/02/10 16:03:08 jlang Exp $
'// Description: Changes Text to Hyperlink
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


' Searches for text of the form '[LinkText(LinkURL)]' and applies the following
' operations:
' - Removes LinkURL including ()
' - Adds a Hyperlink with LinkText as Anchor and LinkURL as address
Sub ChangeToHyperlink()

    Dim linkAddress As String
    Dim linkText As String
    Dim searchPatterns(3) As Variant
    Dim searchPattern As Variant
    
    searchPatterns(0) = "\(http://(*)\)"
    searchPatterns(1) = "\(ftp://(*)\)"
    searchPatterns(2) = "\(\.\./(*)\)"
    
    For Each searchPattern In searchPatterns
    
        ' scan document until no more patterns found
        Do
            ' find searchPattern
            Selection.Find.ClearFormatting
            With Selection.Find
                .Text = searchPattern
                .Forward = True
                .Wrap = wdFindContinue
                .Format = True
                .MatchCase = False
                .MatchWholeWord = False
                .MatchAllWordForms = False
                .MatchSoundsLike = False
                .MatchWildcards = True
            End With
            Selection.Find.Execute
            If Len(Selection.Range.Text) = 0 Then
                Exit Do
            End If
            
            ' cut the address text and the parenthesis
            linkAddress = Trim(Selection.Range.Text)
            If linkAddress <> "" Then
                linkAddress = Mid$(linkAddress, 2, Len(linkAddress) - 2)
                Selection.Cut
                Selection.MoveRight Unit:=wdCharacter, Count:=1, Extend:=True
                
                ' hyperlink is of the form '[text(url)]'
                If Selection.Range.Text = "]" Then
                    Selection.Find.ClearFormatting
                    With Selection.Find
                        .Text = "\[(*)\]"
                        .Forward = False
                        .Wrap = wdFindContinue
                        .Format = True
                        .MatchCase = False
                        .MatchWholeWord = False
                        .MatchAllWordForms = False
                        .MatchSoundsLike = False
                        .MatchWildcards = True
                    End With
                    Selection.Find.Execute
                
                ' hyperlink is of the form 'textWithoutBlanks (url)'
                Else
                    ' skip blanks between text and (url)
                    Do
                        Selection.MoveLeft Unit:=wdCharacter, Count:=1, Extend:=False
                        Selection.MoveLeft Unit:=wdCharacter, Count:=1, Extend:=True
                        If Selection.Range.Text <> " " Then
                          Exit Do
                        End If
                    Loop While True
                    
                    Selection.Find.ClearFormatting
                    With Selection.Find
                        .Text = " (*)"
                        .Forward = False
                        .Wrap = wdFindContinue
                        .Format = True
                        .MatchCase = False
                        .MatchWholeWord = False
                        .MatchAllWordForms = False
                        .MatchSoundsLike = False
                        .MatchWildcards = True
                    End With
                    Selection.Find.Execute
                    ' deselect leading blank
                    Selection.Start = Selection.Start + 1
                End If
                
                ' If selected text already contains a hyperlink, leave the link and
                ' do not touch it otherwise word crashes
                If Selection.Range.Hyperlinks.Count = 0 Then
                    linkText = Selection.Range.Text
                    ActiveDocument.Hyperlinks.Add Anchor:=Selection.Range, Address:=linkAddress, SubAddress:="", ScreenTip:="", TextToDisplay:=Selection.Range.Text
                Else
                    MsgBox "Range " & Selection.Range.Text & " already contains a link. Links must be in the form [LinkText(http://...)]"
                    Selection.MoveRight Unit:=wdCharacter, Count:=1
                End If
            End If
        Loop While True
    Next searchPattern
    
End Sub
