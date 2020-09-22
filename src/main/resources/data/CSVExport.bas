Attribute VB_Name = "CSVExport"

' A subroutine that will take each sheet inside an Excel file and save it as a CSV in the corresponding folder.
' Used to easily switch from Excel to CSV
Sub CSVExport()

    Dim WS As Excel.Worksheet
    Dim SaveToDirectory As String
    
    Dim CurrentWorkbook As String
    Dim CurrentFormat As Long
    Dim TrimmedName As String
    
    Dim wsCount As Integer
    
    CurrentWorkbook = ActiveWorkbook.FullName
    CurrentFormat = ActiveWorkbook.FileFormat
    TrimmedName = Replace(ActiveWorkbook.Name, ".xlsm", "") ' If you have macros enabled, the file will likely be .xlsm
    
    wsCount = ActiveWorkbook.Sheets.Count

    'Make sure this directory exists, otherwise you'll get an error in the MkDir function
    SaveToDirectory = "C:\CSV_EXPORT"

    Debug.Print wsCount & " sheets in current workbook"
    
    For Each WS In Application.ActiveWorkbook.Worksheets
        MkDir (SaveToDirectory & TrimmedName)
        Debug.Print "Saving " & WS.Name & " to csv"
        WS.SaveAs SaveToDirectory & TrimmedName & "\" & WS.Name, xlCSV
    Next
    
    MsgBox ("Action completed")

End Sub


Function MkDir(strPath As String)

    With CreateObject("Scripting.FileSystemObject")
        If Not .FolderExists(strPath) Then .CreateFolder strPath
    End With

End Function