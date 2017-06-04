Infinispan Playground
=====================

A small project to learn and explore Infinispan.

Build instructions
==================

```shell
mvn clean package
```

To launch four nodes on a single machine just run the following command in four different terminals:

```shell
mvn exec:exec

```

Usage
-----

Every node will have its own command line interface "attached", which you can use to play with your Data Grid.

Type 'help' on the command line to show a list of commands:

```shell
> help
address
		Address of this cluster node
all
		List all values in the grid.
clear
		Clear all values.
get <key>
		Get an object from the grid.
help
		List of commands.
info
		Information on cache.
key
		Get a key which is affine to this cluster node
loadtest
		Load example values in the grid
local
		List all local values.
locate <key>
		Locate an object in the grid.
primary
		List all local values for which this node is primary.
put <key> <value>
		Put an object (id, value) in the grid.
putIfAbsent|putifabsent|pia <key> <value>
		Put an object (id, value) in the grid if not already present
quit|exit|q|x
		Exit the shell.
replica
		List all local values for which this node is a replica.
routing
		Print routing table.
who <key>
		Which is the role for this node for an object.
```

Create some example values in one of the nodes:

```shell
> loadtest
New entry 5 created in the cache
New entry 9 created in the cache
Data grid loaded with example values.
```

The command loads 20 example values in the grid. The keys will be distributed between the four nodes. 
You will see different notifications in each node.

Now you can play with the commands to see how the values are distributed in the grid and which node is responsible 
for reads and writes.

The all command shows all the <K,V> in the grid 
```shell
> all
9 Frank Zappa
4 Pink Floyd
5 Arctic Monkeys
15 Pearl Jam
14 Queens of the Stone Age
10 Dire Straits
17 Lynyrd Skynyrd
13 Jimi Hendrix
20 Prince
3 Jethro Tull
16 U2
6 Franz Ferdinand
19 Janis Joplin
12 Van Halen
18 AC/DC
1 Led Zeppelin
2 Deep Purple
7 Queen
8 The Police
11 The Who
Cache Size: 20
```

The local command shows only the <K,V> which are local to this node

```shell
> local
9 Frank Zappa
4 Pink Floyd
5 Arctic Monkeys
15 Pearl Jam
14 Queens of the Stone Age
10 Dire Straits
20 Prince
3 Jethro Tull
19 Janis Joplin
12 Van Halen
2 Deep Purple
7 Queen
11 The Who
Cache Size: 20

Local Size: 13
```

The primary command shows only the <K,V> which this node is responsbile for

```shell
> primary
9 Frank Zappa
5 Arctic Monkeys
Cache Size: 20

Primary Size: 2
``` 
 
The replica command shows only the <K,V> which are replicated to this node (not primary) 

```shell
> replica
20 Prince
4 Pink Floyd
3 Jethro Tull
19 Janis Joplin
12 Van Halen
15 Pearl Jam
14 Queens of the Stone Age
10 Dire Straits
2 Deep Purple
7 Queen
11 The Who
Cache Size: 20

Replica Size: 11
``` 
Size of primary + replica must be equal to the size of local.  

The locate command shows all the members of the cluster and their responsibility for a particular key.
For example in this cluster, launching the command from memento-59111 (* means 'this node') you will find that:

memento-27739 is the Primary node, Read Owner and Write Owner for key 1.
memento-59111 is Write Owner, Read Owner and Write Backup for key 1.
 
```shell
> locate 1
Locating key 1
memento-35873
memento-59111 * (RO) (WO) (WB)
memento-27739 (P) (RO) (WO)
memento-43798
``` 

Now you can exit from some of the nodes
```shell
exit
``` 
And see how the <K,V> are rebalanced in the grid