call %~dp0..\env.bat

set user=dummy
set pw=dummy
set db=dummy
set cmisRoot=./isssatislohprod01

%JAVA_HOME%\bin\java.exe -jar productGenerator1604test.jar "-xlsxToProduct" "-url" "%db%" "-user" "%user%" "-password" "%pw%" "-cmisRoot" "%cmisRoot%"