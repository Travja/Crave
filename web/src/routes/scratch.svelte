<script>
    import {title} from "$lib/variables";
    import {onMount} from "svelte";
    import Cropper from "$lib/Cropper.svelte";

    title.set("Scratch");

    let isOpenCVReady = false;
    let src;
    let imgElement;
    let disabled = false;

    onMount(() => {
        // detectCircles = () => {
        //     disabled = true;
        //
        //     // circle detection code
        //     let srcMat = cv.imread('imageCanvas');
        //     let displayMat = srcMat.clone();
        //     let circlesMat = new cv.Mat();
        //
        //     cv.cvtColor(srcMat, srcMat, cv.COLOR_RGBA2GRAY);
        //     cv.HoughCircles(srcMat, circlesMat, cv.HOUGH_GRADIENT, 1, 45, 75, 40, 0, 0);
        //
        //     for (let i = 0; i < circlesMat.cols; ++i) {
        //         let x = circlesMat.data32F[i * 3];
        //         let y = circlesMat.data32F[i * 3 + 1];
        //         let radius = circlesMat.data32F[i * 3 + 2];
        //         let center = new cv.Point(x, y);
        //
        //         // draw circles
        //         cv.circle(displayMat, center, radius, [0, 0, 0, 255], 3);
        //     }
        //
        //     srcMat.delete();
        //     displayMat.delete();
        //     circlesMat.delete();
        //
        //     disabled = false;
        //     cv.imshow('imageCanvas', displayMat);
        // };


        var Module = {
            setStatus: function (text) {
                if (!Module.setStatus.last) Module.setStatus.last = {
                    time: Date.now(),
                    text: ''
                };
                if (text === Module.setStatus.text) return;
                var m = text.match(/([^(]+)\((\d+(\.\d+)?)\/(\d+)\)/);
                var now = Date.now();
                if (m && now - Date.now() < 30) return; // if this is a progress update, skip it if too soon
                if (m) {
                    text = m[1];

                }
                if (text === '') {
                    isOpenCVReady = true;
                    console.log('OpenCV is ready');
                    // onPreprocess();
                }

            },
            totalDependencies: 0,
            monitorRunDependencies: function (left) {
                this.totalDependencies = Math.max(this.totalDependencies, left);
                Module.setStatus(left ? 'Preparing... (' + (this.totalDependencies - left) + '/' + this.totalDependencies + ')' : 'All downloads complete.');
            }
        };
        Module.setStatus('Downloading...');
    });

    // let detectCircles;
</script>
<section>
    <div>OpenCV is ready? {isOpenCVReady}</div>
    <input type="file" id="fileInput" name="file" on:change={(e) => src = URL.createObjectURL(e.target.files[0])}/>
    <br/>
    <img id="imageSrc" alt="No Image" {src} bind:this={imgElement}
         on:load={() => {
              let image = cv.imread(imgElement);
              cv.imshow('imageCanvas', image);
              image.delete();
         }}/>
    <br/>
    <canvas id="imageCanvas"/>

    <br/>
    {#if disabled}
        <div>Loading...</div>
    {/if}
    <button type="button" id="circlesButton" class="btn btn-primary">Circle Detection
    </button>

    <Cropper/>


    <script async src="opencv.js"/>
    <!--    <script src="/jsfeat/jsfeat-min.js"/>-->
    <!--    <script src="/tracking/tracking.min.js"/>-->
</section>
<style>
    section {
        text-align: center;
    }

    img {
        max-width: 50%;
    }
</style>