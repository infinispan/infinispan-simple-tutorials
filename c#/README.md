# C# Infinispan Client. Tutorials

Set of simple tutorial on how to use the C# client.

Requisites:

- .NET C# Client assembly. You can get it either:
	- from source https://github.com/infinispan/dotnet-client
	- from install pack http://infinispan.org/hotrod-clients/

Do one of the following:
1. copy in this directory (aside this README.md) the following files:
	- hotrodcs.dll
	- hotrod.dll
	- hotrod_wrap.dll

2. update dlls location in simple.csproj with the right paths in your file system

You're on the way.

Compile, run, test, improve and share!


HINT THAT CAN SAVE YOU A LOT OF TIME: the .csproj at build time will copy the dlls into the output directory aside the application,
this is the simpliest way to run the example without changing your setting (PATH variable).
As a general rule remeber that the unmanaged libraries (hotrod_wrap.dll and hotrod.dll) must be either in the PATH or in the working directory of the application.
