<!--suppress ALL -->
<script>
    import {onMount} from "svelte";
    import {Utils} from "$lib/cropper/utils.js";
    import {variables} from "$lib/variables.js";
    import {formSubmit} from "$lib/util.js";
    import {spring} from "svelte/motion";

    let margin = {top: 40, right: 40, bottom: 40, left: 40};
    let gTransform = `translate(${margin.left}, ${margin.top})`;

    let imgWidth = 100, imgHeight = 100;
    let background;
    let backgroundWidth = 100;
    let myHeight = 100,
        myWidth = 100;
    let svg,
        g;

    let circleRadius = 7;
    let circles = [];
    let sizes = spring(Array(4).fill(circleRadius));

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

        let newSizes = [...$sizes];
        newSizes[updating.index] = 14;
        sizes.set(newSizes);

        cursor = "grab";
    };

    const stopCircles = (e) => {
        if (!updating) return;

        let newSizes = [...$sizes];
        newSizes[updating.index] = 7;
        sizes.set(newSizes);

        updating = cursor = undefined;
        circles = [...circles];
        dragged();
    };

    const updateCircles = (e) => {
        if (!updating) return;
        let offsetLeft = wrap.getBoundingClientRect().left;
        let offsetTop = wrap.getBoundingClientRect().top;
        let svgDx = offsetLeft + margin.left;
        let svgDy = offsetTop + margin.top;

        updating.x = e.clientX - svgDx;
        updating.y = e.clientY - svgDy;

        let target = targetPoints[updating.index];
        target[0] = updating.x;
        target[1] = updating.y;

        circles = [...circles];
    };

    let round = (num, digits) => {
        return num.toPrecision(digits); //parseFloat?
    };

    const project = (matrix, point) => {
        point = multiply(matrix, [point[0], point[1], 0, 1]);
        return [point[0] / point[3], point[1] / point[3]];
    };

    let timeout;
    const dragged = () => {
        if (timeout)
            clearTimeout(timeout);

        timeout = setTimeout(warpImage, 50);
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
            circles.push({x: dat[0], y: dat[1], index: index++, id: {}}));

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


    let alpha = 1.0;
    let beta = 30;
    const warpImage = (pointsArray = getPoints()) => { // [x1, y1, x2, y2, x3, y3, x4, y4]
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
        cv.cvtColor(output, output, cv.COLOR_RGBA2GRAY);
        cv.convertScaleAbs(output, output, alpha, beta);
        cv.imshow('imageResult', output);

        sourceMat.delete();
        output.delete();
        transformationMatrix.delete();
        srcMatrix.delete();
        destMatrix.delete();

        running = false;
    };

    let finalCanvas;
    // let downloadLink;
    let fileInput;
    let form;
    let submitting = false;
    const saveCroppedImage = () => {
        if (!finalCanvas || !fileInput) return;

        let datUrl = finalCanvas.toDataURL("image/png");

        console.log(datUrl);
        fileInput.value = datUrl;

        submitting = true;
        submitForm();
        // form.submit();

        // let image = datUrl.replace("image/png", "image/octet-stream");
        // downloadLink.href = image;
        //
        // downloadLink.click();
    };

    const newFile = e => url = URL.createObjectURL(e.target.files[0]);

    const checkResize = () => {
        if (!svg) return;
        // console.log(svg.clientWidth, svg.clientHeight);
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

    const submitForm = () => {
        formSubmit(form, (data) => {
            if (data.error) {
                alert("There was a problem processing this receipt.", data.message);
                return;
            }
            console.log(data);
            submitting = false;
            alert("Receipt submitted successfully");
        });
    };

    let fileUpload;
    const upload = () => {
        if (!image && fileUpload) {
            fileUpload.click();
        }
    };

</script>

<section>
    <input accept="image/*" bind:this={fileUpload} id="file-upload" on:change={newFile} type="file"/>
    <div class="container">
        <div bind:clientWidth={backgroundWidth} bind:this={background}
             class="o_image"
             id="background">
            <!--            <img id="sample" src="/cropper/bill.png" alt="bill"/>-->
            <div bind:clientHeight={myHeight} bind:clientWidth={myWidth}
                 bind:this={wrap} class="imgWrapper">
                {#if url}
                    <img alt="Upload a Receipt" bind:this={image} src="{url}"/>
                {:else}
                    <div>Upload a Receipt</div>
                {/if}
            </div>
            <div class="wrapper" on:click={upload} style="{`--hover: ${!url ? 'pointer' : ''}`}">
                <svg bind:this={svg} height="{myHeight}" id="svg"
                     on:mousemove={updateCircles} on:mouseup={stopCircles}
                     style="{cursor ? `cursor: ${cursor}` : ``}"
                     width="{myWidth}">
                    <g bind:this={g} id="window_g"
                       transform="{gTransform}">
                        {#if points}
                            <polyline {points} class="line"/>
                        {/if}
                        {#each circles as circle, i (circle.id)}
                            <circle cx="{circle.x}" cy="{circle.y}" r="{$sizes[i]}"
                                    class="handle" style="{cursor ? `cursor: ${cursor}` : ``}"
                                    on:mousedown={e => downCircle(e, circle)}/>
                        {/each}
                    </g>
                </svg>
            </div>
        </div>
        <div class="p_image">
            <canvas bind:this={imgInit} id="imageInit"></canvas>
            <canvas bind:this={finalCanvas} id="imageResult"
                    style="--width:{finalWidth}px --height={finalHeight}px"/>
        </div>
    </div>

    {#if !url}
        <div>Upload an image to start cropping!</div>
    {/if}
    <div class="buttonContainer">
        <label class="button" for="file-upload">Choose Image</label>
        <div class="button" on:click={saveCroppedImage}>Submit</div>
        <!--        <a id="download" href="#" bind:this={downloadLink} download="Receipt.png"/>-->
    </div>

    <!--    <label for="alpha">Alpha: </label>-->
    <!--    <input type="range" min="0" max="3" step="0.1" id="alpha" bind:value={alpha}/>-->
    <!--    <label for="beta">Beta: </label>-->
    <!--    <input type="range" min="0" max="100" step="1" id="beta" bind:value={beta}/>-->
    <form action="{variables.gateway}/receipt-service/receipt/parsestr"
          bind:this={form} method="post">
        <input bind:this={fileInput} id="file" name="file" type="hidden"/>
    </form>

    {#if submitting}
        <div id="loading">
            <div id="imgWrapper">
                <img src="/loading.gif" id="loadingImg" alt="Loading"/>
            </div>
        </div>
    {/if}
</section>

<style>
    #loading {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    #imgWrapper {
        width: 40vh;
        height: 40vh;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        overflow: hidden;
        box-shadow: 0px 0px 30px 20px #000;
    }

    #loadingImg {
        height: 100%;
    }

    form {
        display: none;
    }

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

    .wrapper:hover {
        cursor: var(--hover);
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
        background: rgba(0, 0, 0, 0.3);
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
        display: flex;
        flex-direction: row;
        justify-content: center;
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

    #file-upload {
        display: none;
    }

    .button {
        box-sizing: border-box;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen-Sans, Ubuntu, Cantarell, "Helvetica Neue", sans-serif;
        font-size: 1em;
        border: none;
        margin: 5px;
    }

    .button:hover {
        cursor: pointer;
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