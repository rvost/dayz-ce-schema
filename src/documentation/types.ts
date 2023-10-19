export type DocumentationLink = {title: string; url: string};

export type DocumentationAssociation = {
    pattern: string;
    links: DocumentationLink[];
};
