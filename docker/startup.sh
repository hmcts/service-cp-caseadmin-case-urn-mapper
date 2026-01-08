#!/usr/bin/env sh
# Script to add ssl trust certs into the current trust keystore before we start our spring boot app
# We need to do this because
# The app will use the certs

# Set source folder
# ( We want to error in Dev but ot in Prod. How to do this ? Is there an env var we can use ? )
# To set this when running the Docker we can ue "docker run img -e "CERTS_DIR=/etc/certs"
if [ -z "$CERTS_DIR" ]; then
    echo "Error - expects \$CERTS_DIR to be set. i.e. export CERTS_DIR="/etc/certs
    echo "Defaulting to /etc/certs"
    export CERTS_DIR="/etc/certs"
fi

export KEYSTORE="$JAVA_HOME/lib/security/cacerts"
if [ ! -f "$KEYSTORE" ]; then
    echo "Error - expects keystore to already exist"
    exit 1
fi

export count=1
echo "Loading certificates from $CERTS_DIR"
for FILE in $(ls $CERTS_DIR)
do
    alias="nonprod$count"
    echo "Adding $CERTS_DIR/$FILE to truststore with alias $alias"
    keytool -importcert -file $FILE -keystore $KEYSTORE -storepass changeit -alias $alias -noprompt
    count=$((count+1))
done

keytool -list -keystore $KEYSTORE -storepass changeit | grep "Your keystore contains"

java -jar $(ls /app/*.jar | grep -v 'plain' | head -n1)

# Some useful keytool commands
#
# Ignore warning "Warning: use -cacerts option to access cacerts keystore"
# keytool useful stuff
# keytool -list -keystore $KEYSTORE -storepass changeit
# Think the finger print for cpp-nonline is  A0:AF:DB:4F:...:CA:DA:14:C6
# The may be lots of certs so need to grep
# keytool -list -keystore $KEYSTORE -storepass changeit | grep "A0:AF:DB"
# keytool -delete -keystore $KEYSTORE -storepass changeit -alias nonprod1
# keytool -delete -keystore $KEYSTORE -storepass changeit -alias mykey
