# iDunno - A Distributed Machine Learning Cluster

## Instructions
- Build the project: `mvn -DskipTests package`
- Run the *Introducer* node: `bash scripts/introducer.sh <port>`
  - For example, to run locally: `bash scripts/introducer.sh 3000`
- Run the *Member* node: `bash scripts/member.sh <port> <introducer-port>`
  - For example, to run locally: `bash scripts/member.sh 3001 127.0.0.1 3000`

## Commands (for members)
> Arguments:
> - `sdfsfilename` - arbitrary string, is the name of the file in SDFS
> - `localfilename` - the Unix-style local file system name
> - `num-versions` - no more than 5
- `join` - join the network
- `leave` - leave the network
- `list_mem` - display the local membership list
- `list_self` - display self information
- `put <localfilename> <sdfsfilename>` - put file from local dir into SDFS or update an existing file
- `get <sdfsfilename> <localfilename>` - fetch file from SDFS to local dir
- `delete <sdfsfilename>` - delete file from SDFS
- `ls <sdfsfilename>` - list all machine (VM) addresses where this file is currently being stored
- `store` - list all files currently being stored at this machine (VM)
- `get-versions <sdfsfilename> <num-versions> <localfilename>` - get all the last `num-version` versions of `sdfsfilename` and store them in `localfilename`