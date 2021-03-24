import com.lehaine.pixelheist.Assets
import com.lehaine.pixelheist.LevelScene
import com.lehaine.pixelheist.World
import com.soywiz.korge.Korge
import com.soywiz.korge.scene.Module
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.Size
import com.soywiz.korma.geom.SizeInt

suspend fun main() = Korge(Korge.Config(module = GameModule))

object GameModule : Module() {
    override val mainScene = LevelScene::class

    override val windowSize: SizeInt = SizeInt(Size(1920, 1080))
    override val size: SizeInt = SizeInt(Size(384, 216))
    override val bgcolor = Colors["#2b2b2b"]

    override suspend fun AsyncInjector.configure() {
        mapSingleton { Assets().apply { init() } }
        mapInstance(World().apply { loadAsync() })
        mapInstance(0) // load first level
        mapPrototype { LevelScene(get(), get(), get()) }
    }
}