#!/bin/bash

APKLIST=($(command ls app/build/outputs/apk/*.apk))
PROD="edu.rutgers.css.Rutgers"
BETA="$PROD.beta"
ALPH="$PROD.alpha"

let "index = 1"

echo "Choose an APK to install:"

for apkfile in "${APKLIST[@]}"
do
    echo -e "\t[$index] $(basename $apkfile)"
    let "index += 1"
done

read CHOICE
let "CHOICE = $((CHOICE-1))"

if [ -n "${APKLIST[$CHOICE]}" ]; then
    adb install -r "${APKLIST[$CHOICE]}"

    if [[ "${APKLIST[$CHOICE]}" == *"production"* ]]; then
	PACKAGE="$PROD"
    elif [[ "${APKLIST[$CHOICE]}" == *"beta"* ]]; then
	PACKAGE="$BETA"
    elif [[ "${APKLIST[$CHOICE]}" == *"alpha"* ]]; then
	PACKAGE="$ALPH"
    fi

    if [ -n "$PACKAGE" ]; then
	adb shell am start -n "$PACKAGE/$PROD.ui.MainActivity"
    fi
else
    echo "Bad option"
fi
