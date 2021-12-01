<!--<script context="module">-->
<!--export const prerender = true;-->
<!--</script>-->

<script>
    import {title} from "$lib/variables";

    title.set("Home");
</script>

<h1 class="underline">Welcome to Crave!</h1>
<section>
    <img src="https://www.rtm.com/wp-content/uploads/2018/05/shopper-marketing.jpg"/>
    <p>
        Crave is your one-stop shop for all of your shopping needs!
        Through use of crowd-sourcing, we keep a database stocked with data about all the things
        <strong>you</strong> buy. Our goal is to provide one location where you can go to
        keep track of your shopping list, find deals, and plan your shopping trips!
    </p>
    <p>
        By utilizing our shopping list feature, you can figure out the single best store to shop at
        in order to get everything you need, <em>or</em> find the combination of stores to get you
        the lowest price on each individual item.
    </p>

<!--    <div class="clear"/>-->
    <h3 class="underline">A few things to keep in mind</h3>
    <h4>This is still in development!</h4>
    <p>
        Though diligent effort has been put in to make the site function well and operate smoothly,
        please keep in mind that the whole system is very much still a work in progress. Bugs will
        still occur. We'd just ask that any feedback you have, please email that to our developers
        (or join our <a href="https://discord.gg/VwrK3jj">Discord</a>) and we'll do our best to
        address the issues at hand.
    </p>

    <h4>There is limited store support!</h4>
    <p>
        Unfortunately, many stores do not offer public APIs, nor do their receipts supply appropriate
        information for us to automatically parse and read. We plan to continue to expand our supported
        stores, but as of right now, Walmart and Target are the only stores whose receipts you can scan
        automatically. Other stores, when added, are available to be used through the manual entry at
        the bottom of the scan page.
    </p>

    <h1 class="underline">Thank you for being a part of such a great community!</h1>
</section>

<style>
    section {
        /*justify-content: center;*/
        flex: 1;
        flex-grow: 1;
    }

    img {
        width: 50%;
        float: right;
        margin-left: 1rem;
    }

    h1 {
        font-size: 2rem;
        text-align: center;
    }

    section > * {
        /*text-align: center;*/
    }

    .underline {
        text-decoration: underline;
    }
</style>