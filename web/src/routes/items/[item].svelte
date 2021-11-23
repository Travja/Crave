<script context="module">
    import {gateway, title} from "$lib/variables";
    import {findCheapest} from "$lib/util";

    title.set("Item Entry");

    let src = "/find-image.svg";
    let items;
    let item;

    export async function load({page, fetch}) {
        const url = `${gateway()}/item-service/item-details/${page.params.item}`;
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
    }
</script>

<script>
    import FullItemPage from "$lib/layouts/FullItemPage.svelte";
    import {onDestroy, onMount} from "svelte";
    import {parseJWT} from "$lib/util";
    import {variables} from "$lib/variables";

    let userIsAdmin = false;
    let unsubscribe = () => {
    };
    onMount(() => {
        unsubscribe = variables.jwt.subscribe(val => {
            let jwt = parseJWT();
            console.log(jwt);
            if (jwt)
                userIsAdmin = jwt.authorities.includes("ADMIN");
        });
    });

    onDestroy(unsubscribe);
</script>

<FullItemPage {item} {items} {src}/>
{#if userIsAdmin}
    <a class="button" href={`/items/edit/${item.item.upc}`}>Edit</a>
{/if}

<style>
    .button {
        display: inline-block;
        margin: 0 auto;
    }
</style>