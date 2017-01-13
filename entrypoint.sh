#!/usr/bin/env bash

energinet_grabber_properties_path=/opt/energinet-grabber/conf/app.properties
params=("$@")
splitted_property="";
key="";
value="";
for i in "${params[@]}"
do
    :
    splitted_property=(${i//=/ });
    key=${splitted_property[0]};
    value=${splitted_property[1]}
    echo $key=$value
    sed -i "s/\($key=\).*\$/\1${value}/" ${energinet_grabber_properties_path}
done
/opt/energinet-grabber/energinet-grabber.sh