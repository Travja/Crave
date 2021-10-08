<!--suppress ALL -->
<script>
    import {onMount} from "svelte";
    import {Utils} from "$lib/cropper/utils.js";
    import {writable} from "svelte/store";

    let imgWidth = 100, imgHeight = 100;
    let backgroundWidth = 100;
    let svg;
    let g;

    let margin = {top: 40, right: 40, bottom: 40, left: 40};
    let gTransform = `translate(${margin.left}, ${margin.top})`;

    let lines = [];

    let c1, c2, c3, c4;

    let updating, uptElm;
    let offset = {x: 0, y: 0};

    let derivedHeight = 100;
    let maxHeight = 200;

    $:  derivedHeight = imgHeight / (imgWidth / backgroundWidth);

    const downCircle = (e, circ) => {
        updating = circ;
        uptElm = e.target;
        let x = $updating.x;
        let y = $updating.y;
        updating.set({x: $updating.x, y: $updating.y, size: 10});
    };

    const stopCircles = (e) => {
        if (!updating) return;

        updating.set({x: $updating.x, y: $updating.y, size: 7});
        updating = undefined;
        uptElm = undefined;
    };


    let wrap;
    const updateCircles = (e) => {
        if (!updating) return;

        let svgDx = background.offsetLeft - window.scrollX + margin.left;
        let svgDy = background.offsetTop - window.scrollY + margin.top;

        let coords = {
            x: e.clientX - svgDx,
            y: e.clientY - svgDy,
            size: 10
        };

        updating.set({...coords});

        let target = targetPoints[uptElm.getAttribute("num")];
        target[0] = $updating.x;
        target[1] = $updating.y;

        dragged();
    };

    let sourcePoints,
        targetPoints,
        line;

    let positions = [];

    let round = (num, digits) => {
        return num.toPrecision(digits); //parseFloat?
    };

    const transformed = () => {
        let a = [], b = [];
        for (let i = 0, n = sourcePoints.length; i < n; ++i) {
            let s = sourcePoints[i],
                t = targetPoints[i];
            a.push([s[0], s[1], 1, 0, 0, 0, -s[0] * t[0], -s[1] * t[0]]);
            b.push(t[0]);
            a.push([0, 0, 0, s[0], s[1], 1, -s[0] * t[1], -s[1] * t[1]]);
            b.push(t[1]);
        }

        let X = solve(a, b, true),
            matrix = [
                X[0], X[3], 0, X[6],
                X[1], X[4], 0, X[7],
                0, 0, 1, 0,
                X[2], X[5], 0, 1
            ].map(x => round(x, 6));

        for (let i = 0; i < lines.length; i++) {
            let d = lines[i];
            positions[i] = "M" + project(matrix, d[0]) + "L" + project(matrix, d[1]);
        }
    };

    const project = (matrix, point) => {
        point = multiply(matrix, [point[0], point[1], 0, 1]);
        return [point[0] / point[3], point[1] / point[3]];
    };

    let queued;
    let timeout;
    const dragged = () => {
        transformed();
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

    const range = (start, end, spacing) => {
        let data = [];
        for (let i = start; i < end; i += spacing)
            data.push(i);
        return data;
    }

    let background;
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

        lines = [];

        let dataPoints = [];

        range(0, width + 1, xSpacing)
            .forEach(x => dataPoints.push([[x, 0], [x, height]]));
        range(0, height + 1, ySpacing)
            .forEach(y => dataPoints.push([[0, y], [width, y]]));

        dataPoints.forEach(line => lines.push(line));

        c1 = writable({
                x: targetPoints[0][0],
                y: targetPoints[0][1],
                size: 7
            },
            /*  {
                  stiffness: 0.1,
                  damping: 0.25
              }*/),
            c2 = writable({
                    x: targetPoints[1][0],
                    y: targetPoints[1][1],
                    size: 7
                },
                /*{
                    stiffness: 0.1,
                    damping: 0.25
                }*/),
            c3 = writable({
                    x: targetPoints[2][0],
                    y: targetPoints[2][1],
                    size: 7
                },
                /*{
                    stiffness: 0.1,
                    damping: 0.25
                }*/),
            c4 = writable({
                    x: targetPoints[3][0],
                    y: targetPoints[3][1],
                    size: 7
                },
                /* {
                     stiffness: 0.1,
                     damping: 0.25
                 }*/);

        transformed();
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

    let utils;
    let url;// = "/cropper/bill.png";
    let img;
    let disabled = true;
    $: if (img && url) {
        img.src = url;
    }
    // setTimeout(() => url = "/receipt.jpg", 10000);

    let finalUrl;
    $: if (url) {
        finalUrl = `url(${url})`;
    }

    onMount(() => {
        img = new Image();
        utils = new Utils('errorMessage');

        img.onload = () => {
            imgWidth = img.width;
            imgHeight = img.height;
            utils.loadImageToCanvas(url, "imageInit");
            setTimeout(setupCropBox, 500);
        };
    });


    let running = false;
    let imgInit;
    let finalWidth = 100;
    let finalHeight = 100;
    const warpImage = (pointsArray) => { // [x1, y1, x2, y2, x3, y3, x4, y4]
        if (running || !imgInit) return;
        running = true;

        const scalePoints = (pointsArray) => {
            const scaleFactor = svg.clientWidth / imgInit.width;

            return pointsArray.map(e => (parseInt(e) + margin.left) / scaleFactor);
        };

        const adjustPoints = (pointsArray) => {
            // const offset = (background.clientHeight - background.clientWidth) / 2;
            // console.log("Offset:", offset);
            // for (let i = 1; i < pointsArray.length; i += 2) {
            //     pointsArray[i] -= offset;
            // }
        };

        finalWidth = Math.max(Math.abs(pointsArray[2] - pointsArray[0]), Math.abs(pointsArray[4] - pointsArray[6]));
        finalHeight = Math.max(Math.abs(pointsArray[1] - pointsArray[7]), Math.abs(pointsArray[3] - pointsArray[5]));
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
</script>

<section>
    <input type="file" id="file-upload" on:change={newFile}/>
    <div class="container">
        <div id="background" class="o_image" style="background-image: {finalUrl}; --height: {derivedHeight}px;"
             bind:this={background}
             bind:clientWidth={backgroundWidth}>
            <!--            <img id="sample" src="/cropper/bill.png" alt="bill"/>-->
            <div class="wrapper"
                 bind:this={wrap}>
                <svg id="svg" bind:this={svg} height="{derivedHeight}"
                     width="{backgroundWidth ? backgroundWidth : 100}"
                     on:mousemove={updateCircles}
                     on:mouseup={stopCircles}>
                    <g id="window_g" bind:this={g}
                       transform="{gTransform}">
                        {#if c1}
                            <circle cx="{$c1.x}" cy="{$c1.y}" r="{$c1.size}"
                                    num="0" class="handle"
                                    on:mousedown={(e) => downCircle(e, c1)}/>
                            <circle cx="{$c2.x}" cy="{$c2.y}" r="{$c2.size}"
                                    num="1" class="handle"
                                    on:mousedown={(e) => downCircle(e, c2)}/>
                            <circle cx="{$c3.x}" cy="{$c3.y}" r="{$c3.size}"
                                    num="2" class="handle"
                                    on:mousedown={(e) => downCircle(e, c3)}/>
                            <circle cx="{$c4.x}" cy="{$c4.y}" r="{$c4.size}"
                                    num="3" class="handle"
                                    on:mousedown={(e) => downCircle(e, c4)}/>
                        {/if}
                        {#each lines as line, i}
                            <path class="line line--x" d="{positions[i]}"></path>
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

    {#if !finalUrl}
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
    .wrapper {
        display: inline-block;
        overflow: hidden;
    }

    svg {
        position: relative;
        width: 100%;
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

    #background {
        background-position: center;
        background-repeat: no-repeat;
        background-size: contain;
        height: var(--height);
        /*max-height: 100%;*/
    }

    section {
        display: block;
    }

    .container {
        /*position: relative;*/
        display: flex;
        margin-bottom: 10px;
        /*max-height: 70vh;*/
        overflow: hidden;
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
    }

    #imageResult {
        width: var(--width);
        height: var(--height);
    }

    img {
        border: 1px solid blue;
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
        stroke: blue;
        stroke-width: 0.7px;
        stroke-linecap: square;
        opacity: 0.8;
    }


</style>