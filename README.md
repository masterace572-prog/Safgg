# Ludo Vault

A premium offline Ludo game for Android that encourages saving money. Built with Kotlin, Jetpack Compose, and Material Design 3.

## Features

- **Full Ludo Gameplay**: Traditional Ludo with one human player and three AI bots
- **Smart AI**: Rule-based bots that capture, avoid danger, and play strategically
- **Coin Economy**: Virtual coin system with stakes, wins, and losses
- **UPI Recharge**: Generate UPI QR codes to recharge your own wallet
- **Statistics**: Track wins, losses, matches, and highest coin balance
- **Settings**: Customize UPI ID, theme, and sound preferences
- **Beautiful UI**: Premium minimal design with smooth animations

## Tech Stack

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM + Repository Pattern
- Jetpack DataStore
- ZXing (QR generation)

## Architecture

```
com.ludovault
├── data          # Models, DataStore, Repository
├── game          # Game Engine, AI, Board Config
├── ui            # Screens, Components, ViewModels, Theme, Navigation
└── utils         # QR Generator, Constants
```

## Note

This is NOT a gambling or betting app. Virtual coins have no monetary value. The app encourages users to save money into their own savings account through psychological reinforcement.

## License

MIT
