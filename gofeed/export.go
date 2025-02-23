package gofeedandroid

import (
	"bytes"
	"encoding/json"

	"github.com/mmcdole/gofeed"
)

func ParseBodyString(body string) ([]byte, error) {
	fp := gofeed.NewParser()
	// Use custom translator to work around an image quirk
	fp.RSSTranslator = NewCustomRssTranslator()

	feed, err := fp.ParseString(body)

	if err != nil {
		return nil, err
	}

	b, err := json.Marshal(feed)

	return b, err
}

func ParseBodyBytes(body []byte) ([]byte, error) {
	fp := gofeed.NewParser()
	// Use custom translator to work around an image quirk
	fp.RSSTranslator = NewCustomRssTranslator()

	feed, err := fp.Parse(bytes.NewReader(body))

	if err != nil {
		return nil, err
	}

	b, err := json.Marshal(feed)

	return b, err
}
