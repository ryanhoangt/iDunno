# iDunno - A Distributed Machine Learning Cluster

## Instructions
- Build the project: `mvn -DskipTests package`
- Run the *Introducer* node: `bash scripts/introducer.sh <port>`
  - For example, to run locally: `bash scripts/introducer.sh 3000`
- Run the *Member* node: `bash scripts/member.sh <port> <introducer-port>`
  - For example, to run locally: `bash scripts/member.sh 3001 127.0.0.1 3000`
