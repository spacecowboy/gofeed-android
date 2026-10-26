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

URL="https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/com.nononsenseapps.gofeed?publishing_type=automatic"
AUTH="$(printf '%s' "$NEXUS_USERNAME:$NEXUS_PASSWORD" | base64 -w0)"

upload() {
    # Extra curl args (e.g. --ipv4) are passed through as arguments.
    # --fail-with-body makes curl exit non-zero on HTTP >= 400 but still print the body.
    curl --silent --show-error --fail-with-body \
        --request POST \
        --url "$URL" \
        --header "Authorization: Bearer $AUTH" \
        "$@"
}

if upload; then
    echo
    echo "Upload succeeded"
else
    echo >&2
    echo "Upload failed, retrying with IPv4 forced..." >&2
    upload --ipv4
    echo
    echo "Upload succeeded (IPv4)"
fi

echo
echo "Now go here: https://central.sonatype.com/publishing/deployments"
