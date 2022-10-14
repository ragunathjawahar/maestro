package dev.mobile.maestro

import android.app.UiAutomation
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.stub.StreamObserver
import maestro_android.MaestroAndroid
import maestro_android.MaestroDriverGrpc
import maestro_android.deviceInfo
import maestro_android.locationResponse
import maestro_android.tapResponse
import maestro_android.viewHierarchyResponse
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import kotlin.system.measureTimeMillis

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MaestroDriverService {

    @Test
    fun grpcServer() {
        Configurator.getInstance()
            .setActionAcknowledgmentTimeout(0L)
            .setWaitForIdleTimeout(0L)
            .setWaitForSelectorTimeout(0L)

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val uiDevice = UiDevice.getInstance(instrumentation)
        val uiAutomation = instrumentation.uiAutomation

        NettyServerBuilder.forPort(7001)
            .addService(Service(uiDevice, uiAutomation))
            .build()
            .start()

        while (!Thread.interrupted()) {
            Thread.sleep(100)
        }
    }

}

class Service(
    private val uiDevice: UiDevice,
    private val uiAutomation: UiAutomation,
) : MaestroDriverGrpc.MaestroDriverImplBase() {

    override fun deviceInfo(
        request: MaestroAndroid.DeviceInfoRequest,
        responseObserver: StreamObserver<MaestroAndroid.DeviceInfo>
    ) {
        responseObserver.onNext(
            deviceInfo {
                widthPixels = uiDevice.displayWidth
                heightPixels = uiDevice.displayHeight
            }
        )
        responseObserver.onCompleted()
    }

    override fun viewHierarchy(
        request: MaestroAndroid.ViewHierarchyRequest,
        responseObserver: StreamObserver<MaestroAndroid.ViewHierarchyResponse>
    ) {
        val stream = ByteArrayOutputStream()

        Log.d("Maestro", "Requesting view hierarchy")
        val ms = measureTimeMillis {
            ViewHierarchy.dump(
                uiDevice,
                uiAutomation,
                stream
            )
        }
        Log.d("Maestro", "View hierarchy received in $ms ms")

        responseObserver.onNext(
            viewHierarchyResponse {
                hierarchy = stream.toString(Charsets.UTF_8.name())
            }
        )
        responseObserver.onCompleted()
    }

    override fun tap(
        request: MaestroAndroid.TapRequest,
        responseObserver: StreamObserver<MaestroAndroid.TapResponse>
    ) {
        uiDevice.click(
            request.x,
            request.y
        )

        responseObserver.onNext(tapResponse {})
        responseObserver.onCompleted()
    }

    override fun location(
        request: MaestroAndroid.LocationRequest,
        responseObserver: StreamObserver<MaestroAndroid.LocationResponse>,
    ) {
        Log.d("Maestro", "Setting location ${request.latitude}, ${request.longitude}")

        responseObserver.onNext(locationResponse {})
        responseObserver.onCompleted()
    }
}
