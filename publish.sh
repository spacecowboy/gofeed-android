#!/bin/bash -eu

GRADLE_PROPERTIES="$HOME/.gradle/gradle.properties"

read_gradle_property() {
    local key="$1"
    if [[ -f "$GRADLE_PROPERTIES" ]]; then
        sed -n -E "s/^${key}=(.*)$/\1/p" "$GRADLE_PROPERTIES" | tail -n1
    fi
}

if [[ -z "${NEXUS_USERNAME:-}" ]]; then
    NEXUS_USERNAME="$(read_gradle_property nexusUsername)"
fi

if [[ -z "${NEXUS_PASSWORD:-}" ]]; then
    NEXUS_PASSWORD="$(read_gradle_property nexusPassword)"
fi

export NEXUS_USERNAME NEXUS_PASSWORD

if [[ -z "${NEXUS_USERNAME:-}" ]]; then
    echo "Error: NEXUS_USERNAME is not set (checked env and $GRADLE_PROPERTIES)" >&2
    exit 1
fi

if [[ -z "${NEXUS_PASSWORD:-}" ]]; then
    echo "Error: NEXUS_PASSWORD is not set (checked env and $GRADLE_PROPERTIES)" >&2
    exit 1
fi

./gradlew publishOssrhPublicationToOssrhRepository

curl --request POST \
    --url "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.nononsenseapps.gofeed?publishing_type=user_managed" \
    --header "Authorization: Bearer $(printf '%s' "$NEXUS_USERNAME:$NEXUS_PASSWORD" | base64 -w0)"


echo "Now go here: https://central.sonatype.com/publishing/deployments"
