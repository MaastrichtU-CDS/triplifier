############ set properties ############
if [ -z "$SLEEPTIME" ]; then
    SLEEPTIME=0
    echo "SLEEPTIME set to $SLEEPTIME seconds"
    export SLEEPTIME
fi

if [ -z "$XMX" ]; then
    XMX="-Xmx2g"
    echo "XMX set to $XMX"
    export XMX
fi

if [ -z "$ARGUMENTS" ]; then
    ARGUMENTS=""
    echo "ARGUMENTS set to $ARGUMENTS"
    export ARGUMENTS
fi

############ run script ############
if [ $SLEEPTIME = 0 ]; then
    java $XMX -jar triplifier.jar -c $ARGUMENTS
else
    while true
    do
        java $XMX -jar triplifier.jar -c $ARGUMENTS
        echo "================================== SLEEP =================================="
        sleep $SLEEPTIME
    done
fi
