package gofeedandroid

import (
	"bytes"
	"encoding/json"

	"github.com/mmcdole/gofeed"
)

func parseBytes(fb []byte) ([]byte, error) {
	var (
		parsedFeed *gofeed.Feed
		err        error
	)

	fp := gofeed.NewParser()
	// Use custom translator to work around an image quirk
	fp.RSSTranslator = NewCustomRssTranslator()

	parsedFeed, err = fp.Parse(bytes.NewReader(fb))

	if err != nil {
		return nil, err
	}

	b, err := json.Marshal(parsedFeed)

	return b, err
}
