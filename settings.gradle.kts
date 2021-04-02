import java.net.URI

sourceControl {
    gitRepository(URI.create("https://github.com/JetBrains-Research/psiminer.git")) {
        producesModule("org.jetbrains.research.psiminer:psiminer")
    }
}
