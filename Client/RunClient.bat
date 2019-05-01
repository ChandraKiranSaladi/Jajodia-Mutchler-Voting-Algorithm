@ECHO ON

cd "C:\Users\ehaacls\OneDrive - The University of Texas at Dallas\CS 6378 ( Advanced Operating Systems )\Projects\Jajodia-Mutchler-Voting-Algorithm\Client"
javac -cp ".\bin" -d ".\bin" .\src\*.java

FOR /L %%A IN (0,1,0) DO (
  ECHO %%A
  start "%%A" cmd.exe /k "java -cp ".\bin" InvokeMain %%A"
)
cmd /k