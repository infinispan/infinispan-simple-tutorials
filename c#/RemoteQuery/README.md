# C# Infinispan Client. Tutorials

Set of simple tutorial on how to use the C# client.

Requisites:

- .NET C# Client assembly. You can get it either:
	- from source https://github.com/infinispan/dotnet-client
	- from install pack http://infinispan.org/hotrod-clients/

Do one of the following (1,2 may be suitable for devs while 3 is for runtime):

1. copy in this directory (aside this README.md) the following dlls:
	- hotrodcs.dll
	- hotrod.dll
	- hotrod_wrap.dll
Only for 8.0.x version (8.1 is using schannel)
	- libeay32.dll (SSL stuff)
	- ssleay32.dll (SSL stuff)

2. update dlls location in simple.csproj with the right paths in your file system

3. install the binary client on the machine, update the PATH env. variable to include the bin and lib directories of the installed pack, install your application.

You're on the way.

Compile, run, test, improve and share!


HINT THAT CAN SAVE YOU A LOT OF TIME: the .csproj at build time will copy the dlls into the output directory aside the application,
this is the simpliest way to run the example without changing your setting (PATH variable).
As a general rule remeber that the unmanaged libraries (hotrod_wrap, hotrod, libeay32, libssl32) must be either in the PATH or in the application's working directory.
