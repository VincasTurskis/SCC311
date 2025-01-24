To build: run build.sh

Ensure rmiregistry is running in bin/server

in bin/server: java -cp "../../lib/jgroups-3.6.20.Final.jar;." BackendServer

in bin/server: java -cp "../../lib/jgroups-3.6.20.Final.jar;." FrontendServer
    The frontend server will attempt to create some initial/test auctions on startup. Ensure at least 1 backend server is running

in bin/client: java -cp "../../lib/jgroups-3.6.20.Final.jar;." Client
    possible arguments:
    -verbose - prints every received signature, not just the ones that don't match
    -fake - connect to the fake frontend server. Ensure both fake and real frontend servers are running

in bin/server: java -cp "../../lib/jgroups-3.6.20.Final.jar;." FakeFrontendServer
    possible arguments:
    -resign - sign every message with own fake private key instead of keeping the original signature.