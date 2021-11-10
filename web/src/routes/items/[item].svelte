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
            console.log(item);
            return item, items;
        }

        console.log(res);
    }
</script>

<script>
    import FullItemPage from "$lib/layouts/FullItemPage.svelte";
</script>

<FullItemPage {item} {items} {src}>
    <div>{@html item.item.description ? item.item.description?.replaceAll("\n", "<br/>") :
        "No description"}</div>
</FullItemPage>