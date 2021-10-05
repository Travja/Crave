<!--<script context="module">-->
<!--    export const prerender = true;-->
<!--</script>-->
<script>
    import {title, variables} from "$lib/variables";
    import {onMount} from "svelte";
    import PageItem from "$lib/PageItem.svelte";

    title.set("Items");

    let gateway = "...";
    let items;

    onMount(async () => {
        gateway = variables.gateway ? variables.gateway : window.location.origin;

        const getItems = async () => {
            const res = await fetch(gateway + '/item-service/item-details');
            if (res.ok) {
                items = await res.json();
                console.log("Items: ", items);
                return;
            }

            const error = res.json();
            console.error("Had a hard time parsing json.");
            console.log(error);
        };

        await getItems();
    });
</script>
<section>
    <h1>Directory</h1>
    <p>Finding the lowest prices has never been so easy!</p>
    <hr>
    {#if items}
        <div id="items">
            {#each items as item}
                <PageItem {...item}/>
            {/each}
        </div>
    {:else}
        <p id="no-items">No items available.</p>
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

    #items {
        position: relative;
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        justify-content: center;
        align-items: center;
    }

    #no-items {
        color: #666;
        margin-left: 10px;
    }

    hr {
        width: 30%;
        margin: 0.5em 0;
    }
</style>