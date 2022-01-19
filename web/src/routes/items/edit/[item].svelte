<script context="module">
    import {title} from "$lib/variables";

    title.set("Item Entry");

    let itemId;

    export async function load(page) {
        console.log(page);
        return itemId = page.params.item;
    }
</script>

<script>
    import FullItemPage from "$lib/layouts/FullItemPage.svelte";
    import {onMount} from "svelte";
    import {findCheapest, parseJWT} from "$lib/util";
    import {goto} from "$app/navigation";
    import {gateway} from "$lib/variables.js";

    let src = "/find-image.svg";
    let items;
    let item;

    onMount(async () => {
        let jwt = parseJWT();
        if (!jwt || !jwt.authorities.includes("ADMIN"))
            goto("/items/" + item.item.upc, {replaceState: true});

        const url = `${gateway()}/item-service/item-details/${itemId}`;
        console.log(url);
        const res = await fetch(url);

        if (res.ok) {
            let json = await res.json();
            console.log("test");
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
    // let imgUpload;
    //
    // const changeImg = e => {
    //     src = URL.createObjectURL(e.target.files[0]);
    // };
</script>

{#if item}
    <FullItemPage editable="true" {item} {items} {src}/>
{/if}