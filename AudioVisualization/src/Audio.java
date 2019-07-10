
import com.synthbot.jasiohost.*;

import org.jtransforms.fft.*;
//import processing.core.PApplet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Audio implements AsioDriverListener {

    private AsioDriver asioDriver;
    private Set<AsioChannel> activeInputs;
    private HashMap<String, float[]> allInputs = new HashMap<>();
    private int sampleIndex;
    private int bufferSize;
    private double sampleRate;
    public float[] inputL;
    public float[] inputR;
    private float[] inputL2;
    private float[] inputR2;
    public float[] fftInputL;
    public float[] fftInputR;
    private float[] fftInputL2;
    private float[] fftInputR2;
    private float[] fftInputL_prevFrame;
    private float[] fftInputR_prevFrame;
    private float bassSum;
    private float hiSum;
    private FloatFFT_1D fft;

    public float smoothingFactor = 0.015f;

    public void initAudio() {
        System.out.println("Method called from Setup, a New Audio Object was created");
        List<String> drivers = AsioDriver.getDriverNames();
        System.out.println(drivers.get(1));
        asioDriver = AsioDriver.getDriver(drivers.get(1));
//        asioDriver.openControlPanel();
        System.out.println(asioDriver.getBufferPreferredSize());
        System.out.println(asioDriver.getLatencyInput());
        asioDriver.addAsioDriverListener(this);
        bufferSize = asioDriver.getBufferPreferredSize();
        System.out.println(asioDriver.getCurrentState());
        activeInputs = new HashSet<>();
        activeInputs.add(asioDriver.getChannelInput(0));
        activeInputs.add(asioDriver.getChannelInput(1));
        activeInputs.add(asioDriver.getChannelInput(2));
        activeInputs.add(asioDriver.getChannelInput(3));
        allInputs.put(asioDriver.getChannelInput(0).getChannelName(), new float[bufferSize]);
        allInputs.put(asioDriver.getChannelInput(1).getChannelName(), new float[bufferSize]);
        sampleIndex = 0;
        sampleRate = asioDriver.getSampleRate();
        inputL = new float[bufferSize];
        inputR = new float[bufferSize];
        inputL2 = new float[bufferSize];
        inputR2 = new float[bufferSize];
        fftInputL = new float[bufferSize];
        fftInputR = new float[bufferSize];
        fftInputL2 = new float[bufferSize];
        fftInputR2 = new float[bufferSize];
        fftInputL_prevFrame = new float[bufferSize];
        fftInputR_prevFrame = new float[bufferSize];
        fft = new FloatFFT_1D(bufferSize);
        asioDriver.createBuffers(activeInputs);
        asioDriver.start();

    }

    /**
     * The sample rate has changed. This may be due to a user initiated change, or a change in input/output
     * source.
     *
     * @param sampleRate The new sample rate.
     */

    @Override
    public void sampleRateDidChange(double sampleRate) {

    }

    /**
     * The driver requests a reset in the case of an unexpected failure or a device
     * reconfiguration. As this request is being made in a callback, the driver should
     * only be reset after this callback method has returned. The recommended way to reset
     * the driver is:
     * <pre><code>
     * public void resetRequest() {
     *   new Thread() {
     *     @Override
     *     public void run() {
     * 	     AsioDriver.getDriver().returnToState(AsioDriverState.INITIALIZED);
     *     }
     *   }.start();
     * }
     * </code></pre>
     * Because all methods are synchronized, this approach will safely return the driver
     * to the <code>INITIALIZED</code> state as soon as possible. The buffers must then be recreated
     * and the driver restarted.
     */
    @Override
    public void resetRequest() {

    }

    /**
     * The driver detected audio buffer underruns and requires a resynchronization.
     */
    @Override
    public void resyncRequest() {

    }

    /**
     * The driver has a new preferred buffer size. The host should make an effort to
     * accommodate the driver by returning to the <code>INITIALIZED</code> state and calling
     * <code>AsioDriver.createBuffers()</code>.
     *
     * @param bufferSize The new preferred buffer size.
     */
    @Override
    public void bufferSizeChanged(int bufferSize) {

    }

    /**
     * The input or output latencies have changed. The host is updated with the new values.
     *
     * @param inputLatency  The new input latency in milliseconds.
     * @param outputLatency The new output latency in milliseconds.
     */
    @Override
    public void latenciesChanged(int inputLatency, int outputLatency) {

    }

    /**
     * The next block of samples is ready. Input buffers are filled with new input,
     * and output buffers should be filled at the end of this method.
     *
     * @param sampleTime     System time related to sample position, in nanoseconds.
     * @param samplePosition Sample position since <code>start()</code> was called.
     * @param activeChannels The set of channels which are active and have allocated buffers. Retrieve
     *                       the buffers with <code>AsioChannel.getBuffer()</code>, or use <code>AsioChannel.read()</code>
     */
    @Override
    public void bufferSwitch(long sampleTime, long samplePosition, Set<AsioChannel> activeChannels) {
        for (AsioChannel activeChannel : activeChannels) {
            String in =  activeChannel.getChannelName();
//            System.out.println(allInputs.get(activeChannel.getChannelName()));
            if (in.equals("Input 1")) {
                activeChannel.read(inputL);
            } else if (in.equals("Input 2")){
                activeChannel.read(inputR);
            } else if (in.equals("Input 3")) {
                activeChannel.read(inputL2);
            } else {
                activeChannel.read(inputR2);
            }
        }
        fft.realForward(inputL);
        fft.realForward(inputR);
        fft.realForward(inputL2);
        fft.realForward(inputR2);
        fftInputL_prevFrame = fftInputL.clone();
        fftInputR_prevFrame = fftInputR.clone();
        for (int i = 0; i < inputL.length; i++) {
            fftInputL[i] += (inputL[i] - fftInputL[i]) * smoothingFactor;
            fftInputR[i] += (inputR[i] - fftInputR[i]) * smoothingFactor;
            fftInputL2[i] += (inputL2[i] - fftInputL2[i]) * smoothingFactor;
            fftInputR2[i] += (inputR2[i] - fftInputR2[i]) * smoothingFactor;
        }
        bassSum = 0;
        for (int i = 0; i < 9; i++) {
            bassSum += Math.abs(fftInputL[i]);
        }
        hiSum = 0;
        for (int i = 70; i < 100; i++) {
            hiSum += Math.abs(fftInputR[i]);
        }
    }

}
