<script>
    import {onMount} from "svelte";
    import * as d3 from "https://cdn.skypack.dev/d3@7";

    const attachCropBox = function (imgWidth, imgHeight) {
        console.log('image loaded : ', imgWidth, ' ', imgHeight);
        let margin = {top: 40, right: 40, bottom: 40, left: 40}, width = imgWidth - margin.left - margin.right,
            height = imgHeight - margin.top - margin.bottom;
        let sourcePoints = [[0, 0], [width, 0], [width, height], [0, height]],
            targetPoints = [[0, 0], [width, 0], [width, height], [0, height]];

        let svg = d3.select("#background")
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
            .attr("id", "window_g");

        let line = svg.selectAll(".line")
            .data(d3.range(0, width + 1, 40)
                .map((x) => [[x, 0], [x, height]])
                .concat(d3.range(0, height + 1, 40)
                    .map((y) => [[0, y], [width, y]])
                ))
            .enter().append("path").attr("class", "line line--x");
        let handle = svg.selectAll(".handle")
            .data(targetPoints).enter()
            .append("circle")
            .attr("class", "handle")
            .attr("transform", (d) => "translate(" + d + ")")
            .attr("r", 7)
            .call(d3.drag()/*.origin((d) => ({x: d[0], y: d[1]}))*/
                .on("drag", dragged));
        d3.selectAll("button").datum((d) => JSON.parse(this.getAttribute("data-targets")))
            .on("click", clicked).call(transformed);

        function clicked(d) {
            d3.transition().duration(750).tween("points", function () {
                if (!(d == null)) {
                    let i = d3.interpolate(targetPoints, d);
                    return (t) => {
                        handle.data(targetPoints = i(t))
                            .attr("transform", (d) => "translate(" + d + ")");
                        transformed();
                    };
                }
            });
        }

        function dragged(d) {
            d3.select(this)
                .attr("transform", "translate(" + (d[0] = d3.event.x) + "," + (d[1] = d3.event.y) + ")");
            transformed();
        }

        function transformed() {
            for (let a = [], b = [], i = 0, n = sourcePoints.length; i < n; ++i) {
                let s = sourcePoints[i], t = targetPoints[i];
                a.push([s[0], s[1], 1, 0, 0, 0, -s[0] * t[0], -s[1] * t[0]]), b.push(t[0]);
                a.push([0, 0, 0, s[0], s[1], 1, -s[0] * t[1], -s[1] * t[1]]), b.push(t[1]);
                let X = solve(a, b, true),
                    matrix = [X[0], X[3], 0, X[6], X[1], X[4], 0, X[7], 0, 0, 1, 0, X[2], X[5], 0, 1]
                        .map((x) => d3.round(x, 6));
                line.attr("d", (d) => "M" + project(matrix, d[0]) + "L" + project(matrix, d[1]));
            }
        }

        function project(matrix, point) {
            point = multiply(matrix, [point[0], point[1], 0, 1]);
            return [point[0] / point[3], point[1] / point[3]];
        }

        function multiply(matrix, vector) {
            return [matrix[0] * vector[0] + matrix[4] * vector[1] + matrix[8] * vector[2] + matrix[12] * vector[3], matrix[1] * vector[0] + matrix[5] * vector[1] + matrix[9] * vector[2] + matrix[13] * vector[3], matrix[2] * vector[0] + matrix[6] * vector[1] + matrix[10] * vector[2] + matrix[14] * vector[3], matrix[3] * vector[0] + matrix[7] * vector[1] + matrix[11] * vector[2] + matrix[15] * vector[3]];
        }
    }

    onMount(() => {
        let img = new Image();
        img.onload = function () {
            const imgWidth = img.width;
            const imgHeight = img.height;
            attachCropBox(imgWidth, imgHeight)
        }
        img.src = "/receipt.jpg";
    });

</script>

<div class="container">
    <div id="background" class="o_image">
        <img src="/receipt.jpg" alt="bill"/>
    </div>
    <script src="/numeric-solve.js"></script>
    <!--<script src="./crop_window.js"/>-->
</div>

<style>
    img {
        max-width: 80%;
    }
</style>