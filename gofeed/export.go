package gofeedandroid

func ParseBodyString(body string) ([]byte, error) {
	return ParseBodyBytes([]byte(body))
}

func ParseBodyBytes(body []byte) ([]byte, error) {
	return parseBytes(body)
}
