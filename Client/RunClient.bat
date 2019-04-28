@ECHO ON

cd "C:\Users\kiran\OneDrive - The University of Texas at Dallas\CS 6378 ( Advanced Operating Systems )\Projects\Tree Based Quorum\Client"
javac -cp ".\bin" -d ".\bin" .\src\*.java

FOR /L %%A IN (11,1,15) DO (
  ECHO %%A
  start "%%A" cmd.exe /k "java -cp ".\bin" InvokeMain %%A"
)
cmd /k