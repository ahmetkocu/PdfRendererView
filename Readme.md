# Android PDFRenderer View Library
This library is extension of **[AndroidPdfViewer](https://github.com/barteksc/AndroidPdfViewer)** library. 
I added Android SDK PDF Render method. With this extension, you can view PDF files with PDFRenderer for API 21 and above. Also you can use Pdfium to render PDF directly.
You have two PDF render options for more stabile PDF view.

## Download [![](https://jitpack.io/v/ahmetkocu/PdfRendererView.svg)](https://jitpack.io/#ahmetkocu/PdfRendererView)

Add this to your project's `build.gradle`

```groovy
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

And add this to your module's `build.gradle` 

```groovy
dependencies {
	 implementation 'com.github.ahmetkocu:PdfRendererView:1.0.0'
}
```
