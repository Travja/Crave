<script context="module">
    import {title} from "$lib/variables";

    title.set("Item Entry");

    let src = "/find-image.svg";
    let items;
    let item;
    let itemId;

    export async function load({page, fetch}) {
        return itemId = page.params.item;
    }
</script>

<script>
    import FullItemPage from "$lib/layouts/FullItemPage.svelte";
    import {onDestroy, onMount} from "svelte";
    import {parseJWT} from "$lib/util";
    import {variables} from "$lib/variables";
    import {gateway} from "$lib/variables";
    import {findCheapest} from "$lib/util";

    title.set("Item Entry");

    let src = "/find-image.svg";
    let items;
    let item;

    let userIsAdmin = false;
    let unsubscribe = () => {
    };
    onMount(async () => {
        unsubscribe = variables.jwt.subscribe(val => {
            let jwt = parseJWT();
            console.log(jwt);
            if (jwt)
                userIsAdmin = jwt.authorities.includes("ADMIN");
        });

        const url = `${gateway()}/item-service/item-details/${itemId}`;
        console.log(url);
        const res = await fetch(url);

        if (res.ok) {
            let json = await res.json()
            console.log(json);
            items = json;
            item = findCheapest(items);
            if (!item.item.description)
                item.item.description = "No description";
            item.item.description = item.item.description.replaceAll("\n", "<br/>");
            console.log(item);
            title.set(item.item.name);
            return item, items;
        }

        console.log(res);
    });

    onDestroy(unsubscribe);
</script>

{#if item}
    <FullItemPage {item} {items} {src}/>
{/if}
{#if userIsAdmin}
    <a class="button" href={`/items/edit/${item?.item.upc}`}>Edit</a>
{/if}

<style>
    .button {
        display: inline-block;
        margin: 0 auto;
    }
</style>