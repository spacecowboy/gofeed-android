module github.com/spacecowboy/gofeed-android

go 1.22

require (
	github.com/PuerkitoBio/goquery v1.9.1 // indirect
	github.com/andybalholm/cascadia v1.3.2 // indirect
	github.com/json-iterator/go v1.1.12 // indirect
	github.com/mmcdole/gofeed v1.3.0 // indirect
	github.com/mmcdole/goxpp v1.1.1 // indirect
	github.com/modern-go/concurrent v0.0.0-20180306012644-bacd9c7ef1dd // indirect
	github.com/modern-go/reflect2 v1.0.2 // indirect
	golang.org/x/mobile v0.0.0-20240112133503-c713f31d574b // indirect
	golang.org/x/mod v0.14.0 // indirect
	golang.org/x/net v0.21.0 // indirect
	golang.org/x/text v0.14.0 // indirect
	golang.org/x/tools v0.17.0 // indirect
)

// Use fork of gofeed to include fixes specific for Feeder
replace github.com/mmcdole/gofeed v1.3.0 => github.com/spacecowboy/gofeed v1.3.0-feeder
