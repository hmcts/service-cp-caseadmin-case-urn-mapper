#!/usr/bin/env sh
# Script to add ssl trust certs into the current truststore / keystore before we start our spring boot app
# We use self signed certificates in our dev and test environments so we need to add these to our chain of trust
# The kubernetes startup will load any self signed certificates into /etc/certs
# We could load any certs found in the /etc/certs into the default keystore ... but our kube user does not have permissions
# So we copy the keystore to our local folder and add the certs to that
# And when we start our app, we reference the local keystore by passing "-Djavax.net.ssl.trustStore=$MY_KEYSTORE"
logmsg() {
    SCRIPTNAME=$(basename $0)
    echo "$SCRIPTNAME : $1"
}

logmsg "running and loading certificates ..."
export KEYSTORE="$JAVA_HOME/lib/security/cacerts"
export MY_KEYSTORE="./cacerts"
if [ -z "$CERTS_DIR" ]; then
    logmsg "Warning - expects \$CERTS_DIR to be set. i.e. export CERTS_DIR="/etc/certs
    logmsg "Defaulting to /etc/certs"
    export CERTS_DIR="/etc/certs"
fi

if [ ! -f "$KEYSTORE" ]; then
    logmsg "Error - expects keystore to already exist"
    exit 1
fi
logmsg "Copying keystore to mykeystore $MY_KEYSTORE"
cp $KEYSTORE $MY_KEYSTORE

export count=1
logmsg "Loading certificates from $CERTS_DIR into my keystore $MY_KEYSTORE"
for FILE in $(ls $CERTS_DIR)
do
    alias="mojcert$count"
    logmsg "Adding $CERTS_DIR/$FILE to my keystore with alias $alias"
    keytool -importcert -file $CERTS_DIR/$FILE -keystore $MY_KEYSTORE -storepass changeit -alias $alias -noprompt
    count=$((count+1))
done

keytool -list -keystore $MY_KEYSTORE -storepass changeit | grep "Your keystore contains"

export LOCALJARFILE=$(ls ./build/libs/*.jar 2>/dev/null | grep -v 'plain' | head -n1)
export DOCKERJARFILE=$(ls /app/*.jar 2>/dev/null | grep -v 'plain' | head -n1)
if [ -f "$DOCKERJARFILE" ]; then
    logmsg "Running docker java jarfile $DOCKERJARFILE"
    java -Djavax.net.ssl.trustStore=$MY_KEYSTORE -jar $DOCKERJARFILE
elif [ -f "$LOCALJARFILE" ]; then
    logmsg "Running local java jarfile $LOCALJARFILE"
    java -Djavax.net.ssl.trustStore=$MY_KEYSTORE -jar $LOCALJARFILE
else
    logmsg "ERROR - No jarfile found. Unable to start application"
fi
