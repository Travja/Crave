<!--<script context="module">-->
<!--export const prerender = true;-->
<!--</script>-->

<script>
    import {title} from "$lib/variables";

    title.set("Home");
</script>

<h1 class="underline">Welcome to Crave!</h1>
<em>Shop Smart, Shop Efficient</em>
<section>
    <div class="slide with-img">
        <p>
            Crave is your one-stop shop for all of your shopping needs!
            Through use of crowd-sourcing, we keep a database stocked with data about all the things
            <strong>you</strong> buy. Our goal is to provide one location where you can go to
            keep track of your shopping list, find deals, and plan your shopping trips!
        </p>
        <div class="imgWrapper">
            <img src="https://www.rtm.com/wp-content/uploads/2018/05/shopper-marketing.jpg"/>
        </div>
    </div>
    <div class="slide clear">
        <p>
            Create and organize shopping lists that automatically calculate the best places to go to
            get your items. Whether you want to shop at a single store or are looking for the best bargains,
            we've got you covered!
        </p>
    </div>

    <div class="slide">
        <h3 class="underline center">A few things to keep in mind</h3>
        <hr/>
        <h4>This is still in development!</h4>
        <p>
            Though diligent effort has been put in to make the site function well and operate smoothly,
            please keep in mind that the whole system is very much still a work in progress. Bugs will
            still occur. We'd just ask that any feedback you have, please email that to our developers
            (or join our <a href="https://discord.gg/VwrK3jj">Discord</a>) and we'll do our best to
            address the issues at hand.
        </p>
    </div>

    <div class="slide">
        <h4>There is limited store support!</h4>
        <p>
            Unfortunately, many stores do not offer public APIs, nor do their receipts supply appropriate
            information for us to automatically parse and read. We plan to continue to expand our supported
            stores, but as of right now, Walmart and Target are the only stores whose receipts you can scan
            automatically. Other stores, when added, are available to be used through the manual entry at
            the bottom of the scan page.
        </p>
    </div>

    <h1 class="underline">Thank you for being a part of such a great community!</h1>
</section>

<style>
    section {
        /*justify-content: center;*/
        flex: 1;
        flex-grow: 1;
    }

    em {
        text-align: center;
    }

    img {
        max-width: 100%;
        max-height: 100%;
    }

    .imgWrapper {
        display: flex;
        align-items: center;
        margin: 1rem;
    }

    h1 {
        font-size: 2rem;
        text-align: center;
    }

    h3 {
        font-size: 1.8rem;
    }

    h4 {
        font-size: 1.5em;
    }

    section > * {
        /*text-align: center;*/
    }

    .underline {
        text-decoration: underline;
    }

    p {
        font-size: 1.5rem;
    }

    .slide {
        position: sticky;
        top: 3rem;
        background: var(--bg-color);
        min-height: 40vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        text-align: center;
    }

    .slide:nth-child(2n) {
        background-color: var(--secondary-color);
        box-shadow: 0px 0px 6px #444;
    }

    .slide p {
        padding: 1em;
    }

    hr {
        width: 80%;
        margin: 0 auto;
    }

    .groceries {
        background: url("/vegetables.jpg");
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-attachment: fixed;
        background-repeat: no-repeat;
        background-size: cover;
    }

    .grocWrapper p {
        padding: 3rem 1em;
        box-sizing: border-box;
        max-height: 100%;
        z-index: 1;
        background: rgba(255, 255, 255, 0.7);
        backdrop-filter: blur(8px);
    }

    .grocWrapper {
        background-color: unset !important;
    }

    @media only screen and (min-width: 768px) {
        .with-img {
            flex-direction: row;
        }

        .with-img .imgWrapper, .with-img p {
            flex: 1;
        }
    }
</style>