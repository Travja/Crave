<!--suppress ALL -->
<script>
    import {onMount} from "svelte";
    import {Utils} from "$lib/cropper/utils.js";

    let margin = {top: 40, right: 40, bottom: 40, left: 40};
    let gTransform = `translate(${margin.left}, ${margin.top})`;

    let imgWidth = 100, imgHeight = 100;
    let background;
    let backgroundWidth = 100;
    let myHeight = 100,
        myWidth = 100;
    let svg,
        g;

    let circles = [];

    let updating;
    let offset = {x: 0, y: 0};

    let maxHeight = 200;

    let sourcePoints,
        targetPoints,
        line;

    let cursor;

    let positions = [];

    let wrap;

    let running = false;
    let imgInit;
    let finalWidth = 100;
    let finalHeight = 100;
    let image;

    let utils;
    let url;
    let img;
    let disabled = true;
    $: if (img && url) {
        img.src = url;
    }

    let points = "";

    $: if (circles && circles.length > 0) {
        points = "";
        circles.forEach(circ => points += circ.x + " " + circ.y + " ");
        points += circles[0].x + " " + circles[0].y;
    }

    const downCircle = (e, circ) => {
        updating = circ;
        let x = updating.x;
        let y = updating.y;
        updating.size = 10;
        cursor = "grab";
    };

    const stopCircles = (e) => {
        if (!updating) return;

        updating.size = 7;
        updating = cursor = undefined;
        circles = [...circles];
    };

    const updateCircles = (e) => {
        if (!updating) return;
        let offsetLeft = wrap.getBoundingClientRect().left;
        let offsetTop = wrap.getBoundingClientRect().top;
        let svgDx = offsetLeft - window.scrollX + margin.left;
        let svgDy = offsetTop - window.scrollY + margin.top;


        updating.x = e.clientX - svgDx;
        updating.y = e.clientY - svgDy;

        let target = targetPoints[updating.index];
        target[0] = updating.x;
        target[1] = updating.y;

        circles = [...circles];

        dragged();
    };

    let round = (num, digits) => {
        return num.toPrecision(digits); //parseFloat?
    };

    const project = (matrix, point) => {
        point = multiply(matrix, [point[0], point[1], 0, 1]);
        return [point[0] / point[3], point[1] / point[3]];
    };

    let queued,
        timeout;
    const dragged = () => {
        queued = getPoints();
        if (!timeout) {
            timeout = setTimeout(() => {
                warpImage(queued);
                timeout = undefined;
            }, 1000);
        }
    };

    const multiply = (matrix, vector) => {
        return [
            matrix[0] * vector[0] + matrix[4] * vector[1] + matrix[8] * vector[2] + matrix[12] * vector[3],
            matrix[1] * vector[0] + matrix[5] * vector[1] + matrix[9] * vector[2] + matrix[13] * vector[3],
            matrix[2] * vector[0] + matrix[6] * vector[1] + matrix[10] * vector[2] + matrix[14] * vector[3],
            matrix[3] * vector[0] + matrix[7] * vector[1] + matrix[11] * vector[2] + matrix[15] * vector[3]
        ];
    };

    const setupCropBox = () => {
        // console.log('image loaded : ', imgWidth, ' ', imgHeight)
        let width = wrap.clientWidth - margin.left - margin.right,
            height = wrap.clientHeight - margin.top - margin.bottom;

        sourcePoints = [[0, 0], [width, 0], [width, height], [0, height]],
            targetPoints = [[0, 0], [width, 0], [width, height], [0, height]];

        let cols = Math.round(width / 40),
            rows = Math.round(height / 40);

        let xSpacing = width / cols;
        let ySpacing = height / rows;

        circles = [];
        let index = 0;
        targetPoints.forEach(dat =>
            circles.push({x: dat[0], y: dat[1], size: 7, index: index++, id: {}}));

        setTimeout(dragged, 250);
    };

    const getPoints = () => {
        let pointsArray = [];
        const children = document.querySelectorAll('#window_g .handle');
        children.forEach(e => {
            pointsArray.push(parseInt(e.getAttribute("cx")));
            pointsArray.push(parseInt(e.getAttribute("cy")));

        });

        return pointsArray;
    };

    const warpImage = (pointsArray) => { // [x1, y1, x2, y2, x3, y3, x4, y4]
        if (running || !imgInit) return;
        running = true;
        const scaleFactor = svg.clientWidth / imgInit.width;

        const scalePoints = pointsArray => pointsArray.map(e => (parseInt(e) + margin.left) / scaleFactor);
        const adjustPoints = (pointsArray) => {
            const offset = image.offsetTop;
            // console.log("Offset:", offset);
            for (let i = 1; i < pointsArray.length; i += 2) {
                pointsArray[i] -= offset;
            }
        };

        finalWidth = Math.max(Math.abs(pointsArray[2] - pointsArray[0]), Math.abs(pointsArray[4] - pointsArray[6]))
            / scaleFactor;
        finalHeight = Math.max(Math.abs(pointsArray[1] - pointsArray[7]), Math.abs(pointsArray[3] - pointsArray[5]))
            / scaleFactor;
        // console.log("Final Height:", finalHeight, "Final Width:", finalWidth);
        adjustPoints(pointsArray);
        pointsArray = scalePoints(pointsArray);

        let sourceMat = cv.imread('imageInit');
        let output = new cv.Mat();
        let outputSize = new cv.Size(finalWidth, finalHeight);
        let srcMatrix = cv.matFromArray(4, 1, cv.CV_32FC2, pointsArray);
        let destMatrix = cv.matFromArray(4, 1, cv.CV_32FC2, [0, 0, finalWidth, 0, finalWidth, finalHeight, 0, finalHeight]);
        let transformationMatrix = cv.getPerspectiveTransform(srcMatrix, destMatrix);

        cv.warpPerspective(sourceMat, output, transformationMatrix, outputSize, cv.INTER_LINEAR, cv.BORDER_CONSTANT, new cv.Scalar());
        cv.imshow('imageResult', output);

        sourceMat.delete();
        output.delete();
        transformationMatrix.delete();
        srcMatrix.delete();
        destMatrix.delete();

        running = false;
    };

    let finalCanvas;
    let downloadLink;
    const saveCroppedImage = () => {
        if (!finalCanvas || !downloadLink) return;

        let image = finalCanvas.toDataURL("image/png").replace("image/png", "image/octet-stream");
        downloadLink.href = image;

        downloadLink.click();
    };

    const newFile = e => url = URL.createObjectURL(e.target.files[0]);

    const checkResize = () => {
        if (!svg) return;
        console.log(svg.clientWidth, svg.clientHeight);
        let updated = false;
        for (let circ of circles) {
            let rightBound = circ.x + margin.left + circ.size;
            let lowerBound = circ.y + margin.top + circ.size;
            if (rightBound > svg.clientWidth) {
                circ.x = svg.clientWidth - circ.size - margin.left;
                updated = true;
            }

            if (lowerBound > svg.clientHeight) {
                circ.y = svg.clientHeight - circ.size - margin.top;
                updated = true;
            }

            if (updated) {
                let target = targetPoints[circ.index];
                target[0] = circ.x;
                target[1] = circ.y;
            }
        }
        if (updated) {
            circles = [...circles];
            dragged();
        }
    };

    onMount(() => {
        img = new Image();
        utils = new Utils('errorMessage');

        img.onload = () => {
            imgWidth = img.width;
            imgHeight = img.height;
            utils.loadImageToCanvas(url, "imageInit");
            setTimeout(setupCropBox, 500);
        };

        window.addEventListener("resize", checkResize);
    });
</script>

<section>
    <input type="file" id="file-upload" on:change={newFile}/>
    <div class="container">
        <div id="background" class="o_image"
             bind:this={background}
             bind:clientWidth={backgroundWidth}>
            <!--            <img id="sample" src="/cropper/bill.png" alt="bill"/>-->
            <div class="imgWrapper" bind:this={wrap}
                 bind:clientWidth={myWidth} bind:clientHeight={myHeight}>
                <img src="{url}" bind:this={image}/>
            </div>
            <div class="wrapper">
                <svg id="svg" bind:this={svg} height="{myHeight}"
                     width="{myWidth}" style="{cursor ? `cursor: ${cursor}` : ``}"
                     on:mousemove={updateCircles}
                     on:mouseup={stopCircles}>
                    <g id="window_g" bind:this={g}
                       transform="{gTransform}">
                        {#if points}
                            <polyline {points} class="line"/>
                        {/if}
                        {#each circles as circle (circle.id)}
                            <circle cx="{circle.x}" cy="{circle.y}" r="{circle.size}"
                                    class="handle" style="{cursor ? `cursor: ${cursor}` : ``}"
                                    on:mousedown={e => downCircle(e, circle)}/>
                        {/each}
                    </g>
                </svg>
            </div>
        </div>
        <div class="p_image">
            <canvas id="imageInit" bind:this={imgInit}></canvas>
            <canvas id="imageResult" style="--width:{finalWidth}px --height={finalHeight}px"
                    bind:this={finalCanvas}/>
        </div>
    </div>

    {#if !url}
        <div>Upload an image to start cropping!</div>
    {/if}
    <div class="buttonContainer">
        <label class="button" for="file-upload">Choose Image</label>
        <button class="button" on:click={saveCroppedImage}>Save</button>
        <a id="download" bind:this={downloadLink} download="Receipt.png"/>
    </div>

    <script src="/cropper/numeric-solve.min.js"></script>
    <script async src="/cropper/opencv.js"
            on:load={() => setTimeout(() => disabled = false, 500)}
            on:error={() => console.error("Failed to load opencv")}></script>
</section>

<style>
    .imgWrapper {
        height: 70vh;
        max-height: 70vh;
        position: relative;
        display: flex;
        align-items: center;
    }

    .imgWrapper img {
        max-height: 100%;
        max-width: 100%;
    }

    .wrapper {
        position: absolute;
        width: 100%;
        top: 0;
        left: 0;
        display: inline-block;
        overflow: hidden;
    }

    svg {
        position: relative;
    }

    circle {
        fill: red;
        pointer-events: all;
        cursor: move;
        opacity: 0.6;
        filter: drop-shadow(1px 1px 2px #666);
    }

    #download {
        display: none;
    }

    #imageInit {
        display: none;
        width: 80%;
    }

    #imageResult {
        width: var(--width);
        max-width: 100%;
        max-height: 100%;
    }

    #background {
        background-position: center;
        background-repeat: no-repeat;
        background-size: contain;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    section {
        display: block;
    }

    .container {
        position: relative;
        display: flex;
        margin-bottom: 10px;
        max-height: 70vh;
        overflow: hidden;
        user-select: none;
    }

    .buttonContainer {
        margin: 10px auto;
    }

    .container .o_image {
        width: 50%;
        border-right: 1px solid;
    }

    .container .p_image {
        width: 50%;
        border-left: 1px solid;
        text-align: center;
        display: flex;
        justify-content: center;
        align-items: center;
    }

    input {
        display: none;
    }

    .button {
        box-sizing: border-box;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen-Sans, Ubuntu, Cantarell, "Helvetica Neue", sans-serif;
        padding: 0.4em;
        font-size: 1em;
        border: none;
        background-color: coral;
    }

    .button:hover {
        cursor: pointer;
        background-color: #ff7644;
    }

    .line {
        /*stroke: blue;*/
        /*stroke-width: 0.7px;*/
        /*stroke-linecap: square;*/
        /*opacity: 0.8;*/
        fill: deepskyblue;
        fill-opacity: 0.3;
    }


</style>