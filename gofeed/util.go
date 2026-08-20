package gofeedandroid

import (
	"bytes"
	"encoding/json"
	"errors"

	"github.com/mmcdole/gofeed"
	gofeedjson "github.com/mmcdole/gofeed/json"
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

	// Remove once https://github.com/mmcdole/gofeed/issues/344 is fixed
	if err != nil && errors.Is(err, gofeed.ErrFeedTypeNotDetected) {
		// Can't trust detector to detect json. So just try to parse it as a JSON feed
		parsedFeed, err = parseJSONFeed(fb)
	}

	if err != nil {
		return nil, err
	}

	b, err := json.Marshal(parsedFeed)

	return b, err
}

// gofeed has a bug where JSONFeeds larger than 4096 can't be parsed
// see https://github.com/mmcdole/gofeed/issues/344
func parseJSONFeed(fb []byte) (*gofeed.Feed, error) {
	jp := gofeedjson.Parser{}
	jf, err := jp.Parse(bytes.NewReader(fb))
	if err != nil {
		return nil, err
	}

	defaultJSONTranslator := &gofeed.DefaultJSONTranslator{}
	return defaultJSONTranslator.Translate(jf)
}
