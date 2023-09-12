# ConsoleImageViewer


Už jste někdy hledali na headless (čili bez X, čili bez grafického rozhraní, bez monitoru, bez vnc, …) ikonku? Ne? To se máte:). Já ano. A na potvoru tam nebyly žádné devel balíčky a vlastně jsem ani neměl roota, ale náhodou tam byla java, a to dokonce i s javac.

Výsledný prográmek je jedna jediná třída (dokonce i po kompilaci), jejíž logika, cca 100 řádků, je mnohem jednodušší, než samotné rozumění parametrům. Za výsledky nechť promluví:

dnf install console-image-viewer

a

consoleImageViewer -names -best /usr/share/app-info/icons/fedora/128x128/* (případně s | less -R )

![preview](https://mojefedora.cz/wp-content/uploads/2016/05/console-image-587x1024.png)

from https://mojefedora.cz/poberky-z-fedorky/
