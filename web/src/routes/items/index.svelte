<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    import {title, variables} from "$lib/variables";
    import {onDestroy, onMount} from "svelte";
    import PageItem from "$lib/PageItem.svelte";
    import {overrideFetch} from "$lib/util";

    title.set("Items");

    let gateway;
    let items, error;
    let inProgress = false;

    const getItems = async () => {
        if (!gateway)
            return;

        error = undefined;
        inProgress = true;
        const res = await fetch(gateway + '/item-service/items');
        inProgress = false;
        if (res.ok) {
            items = await res.json();
            console.log("Items: ", items);
            return;
        }

        error = await res.json();
        // console.error("Could not fetch items.");
        // console.error(error);
        switch (error.status) {
            case 401:
                error = "User not authenticated.";
                break;
            case 403:
                error = "User is not allowed to access this resource";
                break;
            case 503:
                error = "Services unavailable";
                break;
            default:
                error = "An unknown error occurred.";
        }
    };

    onMount(async () => {
        gateway = variables.gateway ? variables.gateway : window.location.origin;

        overrideFetch();

        await getItems();
    });

    let unsubscribe = variables.jwt.subscribe(getItems);
    onDestroy(unsubscribe);

</script>
<section>
    <h1>Directory</h1>
    <div id="intro">
        <p>Finding the lowest prices has never been so easy!</p>
        <div class="sp"></div>
        <div id="tip">
            <div>Can't find what you're looking for? Make sure to add it!</div>
            <a alt="Scan a Receipt!" class="button" href="/scratch" id="scan">Scan a Receipt!</a>
        </div>
    </div>
    <hr>
    {#if items}
        <div id="items">
            {#each items as item, i}
                {#if item.details}
                    <PageItem {...item}/>
                    {#if i < items.length - 1}
                        <div class="spacer"/>
                    {/if}
                {/if}
            {/each}
        </div>
    {:else if inProgress}
        <p class="no-items">Fetching items..</p>
    {:else if error}
        <p class="no-items">Could not fetch items. {error}</p>
    {:else}
        <p class="no-items">No items available.</p>
    {/if}

</section>
<style>
    section {
        display: flex;
        flex-direction: column;
        /*justify-content: center;*/
        /*align-items: center;*/
        flex: 1;
        flex-grow: 1;
    }

    h1 {
        font-size: 2rem;
        margin-bottom: 0;
    }

    #intro {
        display: flex;
        align-items: center;
    }

    #items {
        position: relative;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: center;
        align-items: stretch;
    }

    .no-items {
        color: #666;
        margin-left: 10px;
    }

    hr {
        width: 30%;
        margin: 0.5em 0;
    }

    .spacer {
        width: 1px;
        background: black;
        margin: 0 10px;
    }

    #tip {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
    }
</style>