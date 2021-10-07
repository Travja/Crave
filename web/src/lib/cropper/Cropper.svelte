<!--suppress ALL -->
<script>
    import {onMount} from "svelte";
    import {Utils} from "$lib/cropper/utils.js";

    let imgWidth, imgHeight;
    let svg;

    let margin = {top: 40, right: 40, bottom: 40, left: 40}

    const attachCropBox = () => {
        console.log('image loaded : ', imgWidth, ' ', imgHeight)
        let width = imgWidth - margin.left - margin.right,
            height = imgHeight - margin.top - margin.bottom;

        let sourcePoints = [[0, 0], [width, 0], [width, height], [0, height]],
            targetPoints = [[0, 0], [width, 0], [width, height], [0, height]];

        // d3.select("#background").attr("width", width)
        //     .attr("height", height);

        let svg = d3.select("#svg")
            .attr("width", imgWidth)
            .attr("height", imgHeight)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .attr("id", "window_g");

        let line = svg.selectAll(".line")
            .data(d3.range(0, width + 1, margin.left)
                .map(x => [[x, 0], [x, height]])
                .concat(d3.range(0, height + 1, margin.top)
                    .map(y => [[0, y], [width, y]])))
            .enter().append("path")
            .attr("class", "line line--x");

        let handle = svg.selectAll(".handle")
            .data(targetPoints)
            .enter().append("circle")
            .attr("class", "handle")
            .attr("transform", d => "translate(" + d + ")")
            .attr("r", 7)
            .call(d3.behavior.drag()
                .origin(d => ({x: d[0], y: d[1]}))
                .on("drag", dragged));

        function dragged(d) {
            d3.select(this)
                .attr("transform", "translate(" + (d[0] = d3.event.x) + "," + (d[1] = d3.event.y) + ")");
            transformed();
            warpImage(getPoints());
        }

        function transformed() {
            let a = [],
                b = [];
            for (let i = 0, n = sourcePoints.length; i < n; ++i) {
                let s = sourcePoints[i], t = targetPoints[i];
                a.push([s[0], s[1], 1, 0, 0, 0, -s[0] * t[0], -s[1] * t[0]]), b.push(t[0]);
                a.push([0, 0, 0, s[0], s[1], 1, -s[0] * t[1], -s[1] * t[1]]), b.push(t[1]);
            }

            let X = solve(a, b, true),
                matrix = [
                    X[0], X[3], 0, X[6],
                    X[1], X[4], 0, X[7],
                    0, 0, 1, 0,
                    X[2], X[5], 0, 1
                ].map(x => d3.round(x, 6));

            line.attr("d", d => "M" + project(matrix, d[0]) + "L" + project(matrix, d[1]));
        }

        function project(matrix, point) {
            point = multiply(matrix, [point[0], point[1], 0, 1]);
            return [point[0] / point[3], point[1] / point[3]];
        }

        function multiply(matrix, vector) {
            return [
                matrix[0] * vector[0] + matrix[4] * vector[1] + matrix[8] * vector[2] + matrix[12] * vector[3],
                matrix[1] * vector[0] + matrix[5] * vector[1] + matrix[9] * vector[2] + matrix[13] * vector[3],
                matrix[2] * vector[0] + matrix[6] * vector[1] + matrix[10] * vector[2] + matrix[14] * vector[3],
                matrix[3] * vector[0] + matrix[7] * vector[1] + matrix[11] * vector[2] + matrix[15] * vector[3]
            ];
        }

        transformed();
    };

    const getPoints = () => {
        let pointsArray = [];
        const children = document.querySelectorAll('#window_g .handle');
        // console.log(children);
        children.forEach(e => {
            const pos = e.getAttribute('transform');
            // console.dir(pos);
            const point = pos.replace('translate(', '').replace(')', '').split(',');
            pointsArray.push(point[0]);
            pointsArray.push(point[1]);
        })
        // console.log(pointsArray);
        return pointsArray;
    };

    let utils;
    let url = "/cropper/bill.png";
    let img;
    let disabled = true;
    $: if (img) {
        img.src = url;
    }

    onMount(() => {
        img = new Image();
        utils = new Utils('errorMessage');
        utils.loadOpenCv(() => setTimeout(() => disabled = false, 500));

        img.onload = () => {
            imgWidth = img.width;
            imgHeight = img.height;
            utils.loadImageToCanvas(url, "imageInit");
            attachCropBox();
        }
    });


    let running = false;
    let imgInit;
    let finalWidth = 120;
    let finalHeight = 300;
    const warpImage = (pointsArray) => { // [x1, y1, x2, y2, x3, y3, x4, y4]
        if (running || !imgInit) return;
        running = true;

        finalWidth = Math.max(Math.abs(pointsArray[2] - pointsArray[0]), Math.abs(pointsArray[4] - pointsArray[6]));
        finalHeight = Math.max(Math.abs(pointsArray[1] - pointsArray[7]), Math.abs(pointsArray[3] - pointsArray[5]));
        console.log("Final Height:", finalHeight, "Final Width:", finalWidth);

        let src = cv.imread('imageInit');
        const imageWidth = imgInit.width;
        // const svgCropHeight = svg.getAttribute('height');
        const svgCropWidth = svg.getAttribute('width');
        // console.log(pointsArray);
        // console.log('h : ', svgCropHeight, ' w : ', svgCropWidth);
        const scaleFactor = imageWidth / svgCropWidth;
        pointsArray = pointsArray.map(e => {
            const num = (parseInt(e) + margin.left) / scaleFactor;
            return num;
        });
        let dst = new cv.Mat();
        let dsize = new cv.Size(finalWidth, finalHeight);
        let srcTri = cv.matFromArray(4, 1, cv.CV_32FC2, pointsArray);
        let dstTri = cv.matFromArray(4, 1, cv.CV_32FC2, [0, 0, finalWidth, 0, finalWidth, finalHeight, 0, finalHeight]);
        let M = cv.getPerspectiveTransform(srcTri, dstTri);
        cv.warpPerspective(src, dst, M, dsize, cv.INTER_LINEAR, cv.BORDER_CONSTANT, new cv.Scalar());
        // cv.resize(dst, dst, dsize, 0, 0, cv.INTER_AREA);
        cv.imshow('imageResult', dst);
        src.delete();
        dst.delete();
        M.delete();
        srcTri.delete();
        dstTri.delete();
        running = false;
    };


</script>

<svelte.head>
    <link href="/cropper/cropper.css" rel="stylesheet"/>
</svelte.head>

<section>
    <div class="container">
        <div id="background" class="o_image" style="background-image: url({url}); --height: {imgHeight}px;">
            <!--            <img id="sample" src="/cropper/bill.png" alt="bill"/>-->
            <svg id="svg" bind:this={svg} height="{imgHeight}" width="{imgWidth}"/>
        </div>
        <div class="p_image">
            <canvas id="imageInit" bind:this={imgInit}></canvas>
            <canvas id="imageResult" style="--width:{finalWidth}px --height={finalHeight}px"></canvas>
        </div>
    </div>

    <script src="/cropper/qunit-2.0.1.js" type="text/javascript"></script>
    <script src="/cropper/d3.v3.min.js"></script>
    <script src="/cropper/numeric-solve.min.js"></script>
</section>

<style>
    #imageInit {
        display: none;
    }

    #background {
        background-position: center;
        background-repeat: no-repeat;
        background-size: contain;
        height: var(--height);
    }

    section {
        display: block;
    }

    .container {
        display: flex;
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

    button {
        width: 80px;
        height: 30px;
        border: none;
        background-color: coral;
    }

</style>