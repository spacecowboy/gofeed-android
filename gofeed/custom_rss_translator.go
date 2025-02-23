package gofeedandroid

import (
	"fmt"
	"strings"

	"github.com/mmcdole/gofeed"
	"github.com/mmcdole/gofeed/rss"
)

type CustomRssTranslator struct {
	defaultTranslator *gofeed.DefaultRSSTranslator
}

func NewCustomRssTranslator() *CustomRssTranslator {
	t := &CustomRssTranslator{}
	t.defaultTranslator = &gofeed.DefaultRSSTranslator{}
	return t
}

func (ct *CustomRssTranslator) Translate(feed interface{}) (*gofeed.Feed, error) {
	rss, ok := feed.(*rss.Feed)
	if !ok {
		return nil, fmt.Errorf("feed is not *rss.Feed")
	}

	f, err := ct.defaultTranslator.Translate(rss)
	if err != nil {
		return nil, err
	}

	// Override the item image logic
	for i, item := range f.Items {
		if item.Image != nil {
			item.Image = ct.translateItemImage(rss.Items[i])
		}
	}

	return f, nil
}

// Custom image logic to remove the first image from the content
// This matches existing logic in gofeed.DefaultRSSTranslator just the bit
// at the end that parses the article and looks for an image has been removed
func (ct *CustomRssTranslator) translateItemImage(rssItem *rss.Item) *gofeed.Image {
	if rssItem.ITunesExt != nil && rssItem.ITunesExt.Image != "" {
		return &gofeed.Image{URL: rssItem.ITunesExt.Image}
	}
	if media, ok := rssItem.Extensions["media"]; ok {
		if content, ok := media["content"]; ok {
			for _, c := range content {
				if strings.Contains(c.Attrs["type"], "image") || strings.Contains(c.Attrs["medium"], "image") {
					return &gofeed.Image{URL: c.Attrs["url"]}
				}
			}
		}
	}
	for _, enc := range rssItem.Enclosures {
		if strings.HasPrefix(enc.Type, "image/") {
			return &gofeed.Image{URL: enc.URL}
		}
	}
	return nil
}
