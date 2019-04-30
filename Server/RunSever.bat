@ECHO ON

cd "C:\Users\ehaacls\OneDrive - The University of Texas at Dallas\CS 6378 ( Advanced Operating Systems )\Projects\Jajodia-Mutchler-Voting-Algorithm\Server"
javac -cp ".\bin" -d ".\bin" .\src\*.java

FOR /L %%A IN (2,1,8) DO (
  ECHO %%A
  start "%%A" cmd.exe /k "java -cp ".\bin" InvokeMain %%A"
)
cmd /k