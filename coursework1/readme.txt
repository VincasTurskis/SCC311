Ensure rmiregistry is running in bin/server

to build: run build.sh

in bin/server: java -cp "../../lib/jgroups-3.6.20.Final.jar;." BackendServer

in bin/server: java -cp "../../lib/jgroups-3.6.20.Final.jar;." FrontendServer

in bin/client: java -cp "../../lib/jgroups-3.6.20.Final.jar;." Client
    possible arguments:
    -verbose - prints every received signature, not just the ones that don't match
    -fake - connect to the fake frontend server. Ensure both fake and real frontend servers are running

in bin/server: java -cp "../../lib/jgroups-3.6.20.Final.jar;." FakeFrontendServer
    possible arguments:
    -resign - sign every message with own fake private key instead of keeping the original signature.